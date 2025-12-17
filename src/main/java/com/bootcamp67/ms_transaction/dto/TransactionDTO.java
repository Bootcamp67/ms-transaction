package com.bootcamp67.ms_transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

  private String id;
  private String customerId;
  private String transactionType;
  private BigDecimal amount;
  private String currency;

  private String sourceAccountId;
  private String destinationAccountId;
  private String cardId;
  private String creditId;

  private String description;
  private String reference;
  private String channel;
  private String status;

  private LocalDateTime transactionDate;
  private LocalDateTime completedDate;

  private BigDecimal transactionFee;
  private String ipAddress;
  private String location;
}
