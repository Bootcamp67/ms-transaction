package com.bootcamp67.ms_transaction.event.producer;

import com.bootcamp67.ms_transaction.event.TransactionCompletedEvent;
import com.bootcamp67.ms_transaction.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final String TRANSACTION_EVENTS_TOPIC = "transaction-events";

  public Mono<Void> publishTransactionCompleted(TransactionCompletedEvent event) {
    log.info("Publishing transaction completed event: {}", event.getTransactionId());

    TransactionEvent transactionEvent = TransactionEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(TransactionEvent.EventType.TRANSACTION_COMPLETED)
        .transactionId(event.getTransactionId())
        .customerId(event.getCustomerId())
        .timestamp(LocalDateTime.now())
        .payload(event)
        .build();

    return sendEvent(TRANSACTION_EVENTS_TOPIC, event.getTransactionId(), transactionEvent);
  }

  private Mono<Void> sendEvent(String topic, String key, Object event) {
    return Mono.create(sink -> {
      try {
        ListenableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(topic, key, event);

        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
          @Override
          public void onSuccess(SendResult<String, Object> result) {
            log.info("Event sent successfully to topic: {}", topic);
            sink.success();
          }

          @Override
          public void onFailure(Throwable ex) {
            log.error("Error sending event: {}", ex.getMessage(), ex);
            sink.error(ex);
          }
        });
      } catch (Exception e) {
        log.error("Exception sending event: {}", e.getMessage(), e);
        sink.error(e);
      }
    });
  }
}
