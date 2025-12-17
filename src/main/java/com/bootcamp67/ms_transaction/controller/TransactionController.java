package com.bootcamp67.ms_transaction.controller;

import com.bootcamp67.ms_transaction.dto.DepositRequest;
import com.bootcamp67.ms_transaction.dto.TransactionDTO;
import com.bootcamp67.ms_transaction.dto.TransactionResponse;
import com.bootcamp67.ms_transaction.dto.TransferRequest;
import com.bootcamp67.ms_transaction.dto.WithdrawalRequest;
import com.bootcamp67.ms_transaction.service.TransactionService;
import com.bootcamp67.ms_transaction.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;

  @GetMapping
  public Mono<ResponseEntity<Flux<TransactionDTO>>> findAll(ServerWebExchange exchange) {
    if (!SecurityContextUtil.isAdmin(exchange)) {
      log.warn("Unauthorized access attempt to findAll by user: {}",
          SecurityContextUtil.getUsername(exchange));
      return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    return Mono.just(ResponseEntity.ok(transactionService.findAll()));
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<TransactionDTO>> findById(
      @PathVariable String id,
      ServerWebExchange exchange) {

    return transactionService.findById(id)
        .flatMap(transaction -> {
          if (!SecurityContextUtil.canAccessCustomerData(exchange, transaction.getCustomerId())) {
            log.warn("Unauthorized access attempt to transaction {} by user: {}",
                id, SecurityContextUtil.getUsername(exchange));
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).<TransactionDTO>build());
          }
          return Mono.just(ResponseEntity.ok(transaction));
        });
  }

  @GetMapping("/customer/{customerId}")
  public Mono<ResponseEntity<Flux<TransactionDTO>>> findByCustomerId(
      @PathVariable String customerId,
      ServerWebExchange exchange) {

    if (!SecurityContextUtil.canAccessCustomerData(exchange, customerId)) {
      log.warn("Unauthorized access attempt to customer {} transactions by user: {}",
          customerId, SecurityContextUtil.getUsername(exchange));
      return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    return Mono.just(ResponseEntity.ok(transactionService.findByCustomerId(customerId)));
  }

  @GetMapping("/account/{accountId}")
  public Flux<TransactionDTO> findByAccountId(@PathVariable String accountId) {
    return transactionService.findByAccountId(accountId);
  }

  @GetMapping("/card/{cardId}")
  public Flux<TransactionDTO> findByCardId(@PathVariable String cardId) {
    return transactionService.findByCardId(cardId);
  }

  @GetMapping("/credit/{creditId}")
  public Flux<TransactionDTO> findByCreditId(@PathVariable String creditId) {
    return transactionService.findByCreditId(creditId);
  }

  @GetMapping("/customer/{customerId}/date-range")
  public Mono<ResponseEntity<Flux<TransactionDTO>>> findByDateRange(
      @PathVariable String customerId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
      ServerWebExchange exchange) {

    if (!SecurityContextUtil.canAccessCustomerData(exchange, customerId)) {
      log.warn("Unauthorized access attempt to customer {} date range by user: {}",
          customerId, SecurityContextUtil.getUsername(exchange));
      return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    return Mono.just(ResponseEntity.ok(
        transactionService.findByDateRange(customerId, startDate, endDate)));
  }

  @PostMapping("/deposit")
  public Mono<ResponseEntity<TransactionResponse>> deposit(
      @RequestBody @Valid DepositRequest request,
      ServerWebExchange exchange) {

    String customerId = SecurityContextUtil.getCustomerId(exchange);
    log.info("Processing deposit for customer: {} by user: {}",
        customerId, SecurityContextUtil.getUsername(exchange));

    return transactionService.deposit(request, customerId)
        .map(dto -> ResponseEntity.ok(
            TransactionResponse.builder()
                .message("Deposit completed successfully")
                .data(dto)
                .build()
        ));
  }

  @PostMapping("/withdrawal")
  public Mono<ResponseEntity<TransactionResponse>> withdrawal(
      @RequestBody @Valid WithdrawalRequest request,
      ServerWebExchange exchange) {

    String customerId = SecurityContextUtil.getCustomerId(exchange);
    log.info("Processing withdrawal for customer: {} by user: {}",
        customerId, SecurityContextUtil.getUsername(exchange));

    return transactionService.withdrawal(request, customerId)
        .map(dto -> ResponseEntity.ok(
            TransactionResponse.builder()
                .message("Withdrawal completed successfully")
                .data(dto)
                .build()
        ));
  }

  @PostMapping("/transfer")
  public Mono<ResponseEntity<TransactionResponse>> transfer(
      @RequestBody @Valid TransferRequest request,
      ServerWebExchange exchange) {

    String customerId = SecurityContextUtil.getCustomerId(exchange);
    log.info("Processing transfer for customer: {} by user: {}",
        customerId, SecurityContextUtil.getUsername(exchange));

    return transactionService.transfer(request, customerId)
        .map(dto -> ResponseEntity.ok(
            TransactionResponse.builder()
                .message("Transfer completed successfully")
                .data(dto)
                .build()
        ));
  }

  @PostMapping("/{id}/reverse")
  public Mono<ResponseEntity<TransactionResponse>> reverseTransaction(
      @PathVariable String id,
      @RequestParam String reason,
      ServerWebExchange exchange) {

    if (!SecurityContextUtil.isAdmin(exchange)) {
      log.warn("Unauthorized reversal attempt for transaction {} by user: {}",
          id, SecurityContextUtil.getUsername(exchange));
      return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    log.info("Reversing transaction {} by admin: {}",
        id, SecurityContextUtil.getUsername(exchange));

    return transactionService.reverseTransaction(id, reason)
        .map(dto -> ResponseEntity.ok(
            TransactionResponse.builder()
                .message("Transaction reversed successfully")
                .data(dto)
                .build()
        ));
  }
}
