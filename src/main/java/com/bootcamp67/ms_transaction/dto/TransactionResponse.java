package com.bootcamp67.ms_transaction.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {
  private String message;
  private TransactionDTO data;
}
