package com.bootcamp67.ms_transaction.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {

  @KafkaListener(
      topics = "account-events",
      groupId = "transaction-service-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handleAccountEvent(String message) {
    log.info("Received account event: {}", message);
    // TODO: Process account events
  }
}
