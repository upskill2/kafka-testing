package dev.tech.dispatch.handler;

import dev.tech.dispatch.exceptions.NonRetryableException;
import dev.tech.dispatch.exceptions.RetryableException;
import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.service.DispatcherService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static dev.tech.dispatch.service.DispatcherService.ORDER_CREATED_TOPIC;

@Component
@Slf4j
@AllArgsConstructor
@KafkaListener (
        id = "orderConsumerClient",
        topics = "order.created",
        groupId = "dispatcher.order.created.consumer",
        containerFactory = "kafkaListenerContainerFactory")
public class OrderCreatedHandler {

    private final DispatcherService dispatcherService;

    @KafkaHandler
    public void listen (@Header (KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                        @Header (KafkaHeaders.RECEIVED_KEY) String key,
                        @Payload OrderCreated payload) {
        log.info ("Received OrderCreated message: {}. Partition: {}, - key {}", payload, partition, key);
        try {
            dispatcherService.process (key, payload);
        } catch (RetryableException e) {
            log.warn ("Retryable Exception: {}", e.getMessage ());
            throw e;
        } catch (Exception e) {
            log.error ("NonRetryable Error processing OrderCreatedHandler: {}", e.getMessage ());
            throw new NonRetryableException (e);
        }
    }

}
