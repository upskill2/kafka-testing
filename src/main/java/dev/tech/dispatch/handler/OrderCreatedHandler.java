package dev.tech.dispatch.handler;

import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.service.DispatcherService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@AllArgsConstructor
public class OrderCreatedHandler {

    private final DispatcherService dispatcherService;

    @KafkaListener (
            id = "orderConsumerClient",
            topics = "order.created",
            groupId = "dispatcher.order.created.consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen (@Header (KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                        @Header (KafkaHeaders.RECEIVED_KEY) String key,
                        @Payload OrderCreated payload) {
        log.info ("Received OrderCreated message: {}. Partition: {}, - key {}", payload, partition, key);
        try {
            dispatcherService.process (key, payload);
        } catch (Exception e) {
            log.error ("Error processing OrderCreatedHandler: {}", e.getMessage ());
        }
    }

}
