package com.bootcamp67.ms_transaction.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {
  private String transactionId;
  private String customerId;
  private String transactionType;
  private BigDecimal amount;
  private String sourceAccountId;
  private String destinationAccountId;
  private String status;
}
