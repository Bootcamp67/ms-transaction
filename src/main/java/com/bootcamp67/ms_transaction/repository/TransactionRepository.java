package com.bootcamp67.ms_transaction.repository;

import com.bootcamp67.ms_transaction.entity.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction,String> {
  Flux<Transaction> findByCustomerId(String customerId);

  Flux<Transaction> findBySourceAccountId(String accountId);

  Flux<Transaction> findByDestinationAccountId(String accountId);

  Flux<Transaction> findByCardId(String cardId);

  Flux<Transaction> findByCreditId(String creditId);

  Flux<Transaction> findByCustomerIdAndTransactionDateBetween(
      String customerId,
      LocalDateTime startDate,
      LocalDateTime endDate);

  Flux<Transaction> findByTransactionType(String transactionType);

  Flux<Transaction> findByStatus(String status);
}
