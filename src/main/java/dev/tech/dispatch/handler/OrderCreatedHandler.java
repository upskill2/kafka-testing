package dev.tech.dispatch.handler;

import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.service.DispatcherService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
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
    public void listen (OrderCreated payload) {
        log.info ("OrderCreatedHandler: {}", payload);
        try {
            dispatcherService.process (payload);
        } catch (Exception e) {
            log.error ("Error processing OrderCreatedHandler: {}", e.getMessage ());
        }
    }

}
