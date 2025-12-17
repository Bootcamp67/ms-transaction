package com.bootcamp67.ms_transaction.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class TransferRequest {
  @NotBlank(message = "Source account is required")
  private String sourceAccountId;

  @NotBlank(message = "Destination account is required")
  private String destinationAccountId;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  private BigDecimal amount;

  private String description;
  private String currency = "PEN";
  private String channel = "WEB";
}
