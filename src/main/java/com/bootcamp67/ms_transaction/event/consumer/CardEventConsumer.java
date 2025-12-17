package com.bootcamp67.ms_transaction.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardEventConsumer {

  @KafkaListener(
      topics = "payment-events",
      groupId = "transaction-service-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handlePaymentEvent(String message) {
    log.info("Received payment event: {}", message);
    // TODO: Record card payment as transaction
  }
}
