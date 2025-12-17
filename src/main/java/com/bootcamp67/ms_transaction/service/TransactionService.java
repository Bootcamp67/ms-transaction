package com.bootcamp67.ms_transaction.service;

import com.bootcamp67.ms_transaction.dto.DepositRequest;
import com.bootcamp67.ms_transaction.dto.TransactionDTO;
import com.bootcamp67.ms_transaction.dto.TransferRequest;
import com.bootcamp67.ms_transaction.dto.WithdrawalRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionService {
  // Query operations
  Flux<TransactionDTO> findAll();
  Mono<TransactionDTO> findById(String id);
  Flux<TransactionDTO> findByCustomerId(String customerId);
  Flux<TransactionDTO> findByAccountId(String accountId);
  Flux<TransactionDTO> findByCardId(String cardId);
  Flux<TransactionDTO> findByCreditId(String creditId);
  Flux<TransactionDTO> findByDateRange(String customerId, LocalDateTime startDate, LocalDateTime endDate);

  // Transaction operations
  Mono<TransactionDTO> deposit(DepositRequest request, String customerId);
  Mono<TransactionDTO> withdrawal(WithdrawalRequest request, String customerId);
  Mono<TransactionDTO> transfer(TransferRequest request, String customerId);
  Mono<TransactionDTO> reverseTransaction(String transactionId, String reason);
}
