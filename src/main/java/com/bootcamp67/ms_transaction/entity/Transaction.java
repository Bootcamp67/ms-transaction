package com.bootcamp67.ms_transaction.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

  @Id
  private String id;

  private String customerId;
  private String transactionType;     // DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, CREDIT_PAYMENT
  private BigDecimal amount;
  private String currency;            // PEN, USD

  // Source and destination
  private String sourceAccountId;     // Origin account (for transfers/withdrawals)
  private String destinationAccountId; // Destination account (for deposits/transfers)
  private String cardId;              // If transaction is from a card
  private String creditId;            // If transaction is a credit payment

  // Transaction details
  private String description;
  private String reference;           // External reference number
  private String channel;             // WEB, MOBILE, ATM, BRANCH

  // Status and validation
  private String status;              // PENDING, COMPLETED, FAILED, REVERSED
  private LocalDateTime transactionDate;
  private LocalDateTime completedDate;

  // Balances
  private BigDecimal sourceBalanceBefore;
  private BigDecimal sourceBalanceAfter;
  private BigDecimal destinationBalanceBefore;
  private BigDecimal destinationBalanceAfter;

  // Fees and charges
  private BigDecimal transactionFee;
  private String feeType;             // FIXED, PERCENTAGE

  // Metadata
  private String ipAddress;
  private String deviceId;
  private String location;
  private String notes;

  // Audit
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
}
