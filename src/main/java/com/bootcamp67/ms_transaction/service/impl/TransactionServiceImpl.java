package com.bootcamp67.ms_transaction.service.impl;

import com.bootcamp67.ms_transaction.dto.AccountResponse;
import com.bootcamp67.ms_transaction.dto.DepositRequest;
import com.bootcamp67.ms_transaction.dto.TransactionDTO;
import com.bootcamp67.ms_transaction.dto.TransferRequest;
import com.bootcamp67.ms_transaction.dto.WithdrawalRequest;
import com.bootcamp67.ms_transaction.entity.Transaction;
import com.bootcamp67.ms_transaction.exception.InsufficientBalanceException;
import com.bootcamp67.ms_transaction.exception.NotFoundException;
import com.bootcamp67.ms_transaction.exception.TransactionException;
import com.bootcamp67.ms_transaction.repository.TransactionRepository;
import com.bootcamp67.ms_transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
  private final TransactionRepository transactionRepository;
  private final WebClient.Builder webClientBuilder;

  // Microservices URLs
  private static final String ACCOUNT_SERVICE_URL = "http://ms-account:8082";

  // Transaction fees
  private static final BigDecimal WITHDRAWAL_FEE = BigDecimal.valueOf(5.00);
  private static final BigDecimal TRANSFER_FEE = BigDecimal.valueOf(3.00);


  @Override
  public Flux<TransactionDTO> findAll() {
    log.info("Finding all transactions");
    return transactionRepository.findAll()
        .map(this::mapToDTO);
  }

  @Override
  public Mono<TransactionDTO> findById(String id) {
    log.info("Finding transaction by id: {}", id);
    return transactionRepository.findById(id)
        .switchIfEmpty(Mono.error(new NotFoundException("Transaction not found")))
        .map(this::mapToDTO);
  }

  @Override
  public Flux<TransactionDTO> findByCustomerId(String customerId) {
    log.info("Finding transactions for customer: {}", customerId);
    return transactionRepository.findByCustomerId(customerId)
        .map(this::mapToDTO);
  }

  @Override
  public Flux<TransactionDTO> findByAccountId(String accountId) {
    log.info("Finding transactions for account: {}", accountId);
    return Flux.merge(
        transactionRepository.findBySourceAccountId(accountId),
        transactionRepository.findByDestinationAccountId(accountId)
    ).map(this::mapToDTO);
  }

  @Override
  public Flux<TransactionDTO> findByCardId(String cardId) {
    log.info("Finding transactions for card: {}", cardId);
    return transactionRepository.findByCardId(cardId)
        .map(this::mapToDTO);
  }

  @Override
  public Flux<TransactionDTO> findByCreditId(String creditId) {
    log.info("Finding transactions for credit: {}", creditId);
    return transactionRepository.findByCreditId(creditId)
        .map(this::mapToDTO);
  }

  @Override
  public Flux<TransactionDTO> findByDateRange(String customerId, LocalDateTime startDate, LocalDateTime endDate) {
    log.info("Finding transactions for customer {} between {} and {}", customerId, startDate, endDate);
    return transactionRepository.findByCustomerIdAndTransactionDateBetween(customerId, startDate, endDate)
        .map(this::mapToDTO);
  }

  @Override
  public Mono<TransactionDTO> deposit(DepositRequest request, String customerId) {
    log.info("Processing deposit of {} to account {}", request.getAmount(), request.getAccountId());

    // Create transaction
    Transaction transaction = Transaction.builder()
        .id(UUID.randomUUID().toString())
        .customerId(customerId)
        .transactionType("DEPOSIT")
        .amount(request.getAmount())
        .currency(request.getCurrency())
        .destinationAccountId(request.getAccountId())
        .description(request.getDescription())
        .reference(request.getReference())
        .channel(request.getChannel())
        .status("PENDING")
        .transactionDate(LocalDateTime.now())
        .transactionFee(BigDecimal.ZERO)
        .createdAt(LocalDateTime.now())
        .createdBy(customerId)
        .build();

    return transactionRepository.save(transaction)
        .flatMap(savedTransaction ->
            // Call account service to credit account
            creditAccount(request.getAccountId(), request.getAmount())
                .flatMap(accountResponse -> {
                  // Update transaction status
                  savedTransaction.setStatus("COMPLETED");
                  savedTransaction.setCompletedDate(LocalDateTime.now());
                  savedTransaction.setDestinationBalanceAfter(accountResponse.getBalance());
                  savedTransaction.setUpdatedAt(LocalDateTime.now());

                  return transactionRepository.save(savedTransaction);
                })
                .onErrorResume(error -> {
                  // Mark transaction as failed
                  savedTransaction.setStatus("FAILED");
                  savedTransaction.setNotes("Error: " + error.getMessage());
                  savedTransaction.setUpdatedAt(LocalDateTime.now());

                  return transactionRepository.save(savedTransaction)
                      .flatMap(failed -> Mono.error(
                          new TransactionException("Deposit failed: " + error.getMessage())));
                })
        )
        .map(this::mapToDTO)
        .doOnSuccess(tx -> log.info("Deposit completed successfully: {}", tx.getId()))
        .doOnError(error -> log.error("Deposit failed: {}", error.getMessage()));

  }

  @Override
  public Mono<TransactionDTO> withdrawal(WithdrawalRequest request, String customerId) {
    log.info("Processing withdrawal of {} from account {}", request.getAmount(), request.getAccountId());

    BigDecimal totalAmount = request.getAmount().add(WITHDRAWAL_FEE);

    // Create transaction
    Transaction transaction = Transaction.builder()
        .id(UUID.randomUUID().toString())
        .customerId(customerId)
        .transactionType("WITHDRAWAL")
        .amount(request.getAmount())
        .currency(request.getCurrency())
        .sourceAccountId(request.getAccountId())
        .description(request.getDescription())
        .channel(request.getChannel())
        .status("PENDING")
        .transactionDate(LocalDateTime.now())
        .transactionFee(WITHDRAWAL_FEE)
        .feeType("FIXED")
        .createdAt(LocalDateTime.now())
        .createdBy(customerId)
        .build();

    return transactionRepository.save(transaction)
        .flatMap(savedTransaction ->
            // Call account service to debit account
            debitAccount(request.getAccountId(), totalAmount)
                .flatMap(accountResponse -> {
                  // Update transaction status
                  savedTransaction.setStatus("COMPLETED");
                  savedTransaction.setCompletedDate(LocalDateTime.now());
                  savedTransaction.setSourceBalanceAfter(accountResponse.getBalance());
                  savedTransaction.setUpdatedAt(LocalDateTime.now());

                  return transactionRepository.save(savedTransaction);
                })
                .onErrorResume(error -> {
                  // Mark transaction as failed
                  savedTransaction.setStatus("FAILED");
                  savedTransaction.setNotes("Error: " + error.getMessage());
                  savedTransaction.setUpdatedAt(LocalDateTime.now());

                  return transactionRepository.save(savedTransaction)
                      .flatMap(failed -> Mono.error(
                          new TransactionException("Withdrawal failed: " + error.getMessage())));
                })
        )
        .map(this::mapToDTO)
        .doOnSuccess(tx -> log.info("Withdrawal completed successfully: {}", tx.getId()))
        .doOnError(error -> log.error("Withdrawal failed: {}", error.getMessage()));

  }

  @Override
  public Mono<TransactionDTO> transfer(TransferRequest request, String customerId) {
    log.info("Processing transfer of {} from {} to {}",
        request.getAmount(), request.getSourceAccountId(), request.getDestinationAccountId());

    if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
      return Mono.error(new TransactionException("Source and destination accounts must be different"));
    }

    BigDecimal totalAmount = request.getAmount().add(TRANSFER_FEE);

    Transaction transaction = Transaction.builder()
        .id(UUID.randomUUID().toString())
        .customerId(customerId)
        .transactionType("TRANSFER")
        .amount(request.getAmount())
        .currency(request.getCurrency())
        .sourceAccountId(request.getSourceAccountId())
        .destinationAccountId(request.getDestinationAccountId())
        .description(request.getDescription())
        .channel(request.getChannel())
        .status("PENDING")
        .transactionDate(LocalDateTime.now())
        .transactionFee(TRANSFER_FEE)
        .feeType("FIXED")
        .createdAt(LocalDateTime.now())
        .createdBy(customerId)
        .build();

    return transactionRepository.save(transaction)
        .flatMap(savedTransaction ->
            debitAccount(request.getSourceAccountId(), totalAmount)
                .flatMap(sourceResponse ->
                    creditAccount(request.getDestinationAccountId(), request.getAmount())
                        .flatMap(destResponse -> {
                          savedTransaction.setStatus("COMPLETED");
                          savedTransaction.setCompletedDate(LocalDateTime.now());
                          savedTransaction.setSourceBalanceAfter(sourceResponse.getBalance());
                          savedTransaction.setDestinationBalanceAfter(destResponse.getBalance());
                          savedTransaction.setUpdatedAt(LocalDateTime.now());

                          return transactionRepository.save(savedTransaction);
                        })
                        .onErrorResume(creditError -> {
                          log.error("Credit failed, reversing debit: {}", creditError.getMessage());
                          return creditAccount(request.getSourceAccountId(), totalAmount)
                              .then(Mono.error(new TransactionException(
                                  "Transfer failed: " + creditError.getMessage())));
                        })
                )
                .onErrorResume(debitError -> {
                  savedTransaction.setStatus("FAILED");
                  savedTransaction.setNotes("Error: " + debitError.getMessage());
                  savedTransaction.setUpdatedAt(LocalDateTime.now());

                  return transactionRepository.save(savedTransaction)
                      .flatMap(failed -> Mono.error(
                          new TransactionException("Transfer failed: " + debitError.getMessage())));
                })
        )
        .map(this::mapToDTO)
        .doOnSuccess(tx -> log.info("Transfer completed successfully: {}", tx.getId()))
        .doOnError(error -> log.error("Transfer failed: {}", error.getMessage()));
  }

  @Override
  public Mono<TransactionDTO> reverseTransaction(String transactionId, String reason) {
    log.info("Reversing transaction: {}", transactionId);

    return transactionRepository.findById(transactionId)
        .switchIfEmpty(Mono.error(new NotFoundException("Transaction not found")))
        .flatMap(originalTransaction -> {
          if (!"COMPLETED".equals(originalTransaction.getStatus())) {
            return Mono.error(new TransactionException("Only completed transactions can be reversed"));
          }

         Transaction reversal = Transaction.builder()
              .id(UUID.randomUUID().toString())
              .customerId(originalTransaction.getCustomerId())
              .transactionType(originalTransaction.getTransactionType() + "_REVERSAL")
              .amount(originalTransaction.getAmount())
              .currency(originalTransaction.getCurrency())
              .sourceAccountId(originalTransaction.getDestinationAccountId())
              .destinationAccountId(originalTransaction.getSourceAccountId())
              .description("Reversal of transaction: " + transactionId)
              .reference(originalTransaction.getId())
              .channel("SYSTEM")
              .status("COMPLETED")
              .transactionDate(LocalDateTime.now())
              .completedDate(LocalDateTime.now())
              .transactionFee(BigDecimal.ZERO)
              .notes(reason)
              .createdAt(LocalDateTime.now())
              .createdBy("SYSTEM")
              .build();

          originalTransaction.setStatus("REVERSED");
          originalTransaction.setNotes("Reversed: " + reason);
          originalTransaction.setUpdatedAt(LocalDateTime.now());

          return transactionRepository.save(reversal)
              .then(transactionRepository.save(originalTransaction))
              .map(this::mapToDTO);
        })
        .doOnSuccess(tx -> log.info("Transaction reversed successfully: {}", tx.getId()))
        .doOnError(error -> log.error("Reversal failed: {}", error.getMessage()));

  }

  // Helper methods to interact with Account Service
  private Mono<AccountResponse> debitAccount(String accountId, BigDecimal amount) {
    return webClientBuilder.build()
        .post()
        .uri(ACCOUNT_SERVICE_URL + "/api/v1/accounts/" + accountId + "/debit")
        .bodyValue(Collections.singletonMap("amount", amount))
        .retrieve()
        .bodyToMono(AccountResponse.class)
        .onErrorMap(error -> new InsufficientBalanceException("Insufficient balance in account"));
  }

  private Mono<AccountResponse> creditAccount(String accountId, BigDecimal amount) {
    return webClientBuilder.build()
        .post()
        .uri(ACCOUNT_SERVICE_URL + "/api/v1/accounts/" + accountId + "/credit")
        .bodyValue(Collections.singletonMap("amount", amount))
        .retrieve()
        .bodyToMono(AccountResponse.class)
        .onErrorMap(error -> new TransactionException("Failed to credit account"));
  }

  // Helper method to map Transaction to DTO
  private TransactionDTO mapToDTO(Transaction transaction) {
    return TransactionDTO.builder()
        .id(transaction.getId())
        .customerId(transaction.getCustomerId())
        .transactionType(transaction.getTransactionType())
        .amount(transaction.getAmount())
        .currency(transaction.getCurrency())
        .sourceAccountId(transaction.getSourceAccountId())
        .destinationAccountId(transaction.getDestinationAccountId())
        .cardId(transaction.getCardId())
        .creditId(transaction.getCreditId())
        .description(transaction.getDescription())
        .reference(transaction.getReference())
        .channel(transaction.getChannel())
        .status(transaction.getStatus())
        .transactionDate(transaction.getTransactionDate())
        .completedDate(transaction.getCompletedDate())
        .transactionFee(transaction.getTransactionFee())
        .ipAddress(transaction.getIpAddress())
        .location(transaction.getLocation())
        .build();
  }
}
