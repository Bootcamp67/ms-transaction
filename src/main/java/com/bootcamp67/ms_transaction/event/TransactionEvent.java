package com.bootcamp67.ms_transaction.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

  private String eventId;
  private String eventType;
  private String transactionId;
  private String customerId;
  private LocalDateTime timestamp;
  private Object payload;

  public static class EventType {
    public static final String TRANSACTION_CREATED = "TRANSACTION_CREATED";
    public static final String TRANSACTION_COMPLETED = "TRANSACTION_COMPLETED";
    public static final String TRANSACTION_FAILED = "TRANSACTION_FAILED";
    public static final String TRANSACTION_REVERSED = "TRANSACTION_REVERSED";
    public static final String DEPOSIT_COMPLETED = "DEPOSIT_COMPLETED";
    public static final String WITHDRAWAL_COMPLETED = "WITHDRAWAL_COMPLETED";
    public static final String TRANSFER_COMPLETED = "TRANSFER_COMPLETED";
  }
}
