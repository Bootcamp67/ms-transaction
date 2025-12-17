package com.bootcamp67.ms_transaction.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountResponse {
  private String id;
  private BigDecimal balance;
}
