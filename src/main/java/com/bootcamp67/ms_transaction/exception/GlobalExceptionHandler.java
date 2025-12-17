package com.bootcamp67.ms_transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleNotFound(NotFoundException exception) {
    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", exception.getMessage())));
  }

  @ExceptionHandler(InsufficientBalanceException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleInsufficientBalance(InsufficientBalanceException exception) {
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", exception.getMessage())));
  }

  @ExceptionHandler(TransactionException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleTransactionException(TransactionException exception) {
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", exception.getMessage())));
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, String>>> handleGeneral(Exception exception) {
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", exception.getMessage())));
  }
}
