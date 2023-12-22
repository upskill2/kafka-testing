package dev.tech.dispatch.service;

import dev.tech.dispatch.message.DispatchCompletedEvent;
import dev.tech.dispatch.message.DispatchPreparingEvent;
import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.message.OrderDispatched;
import dev.tech.dispatch.restclient.StockServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispatcherService {
    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    public static final String ORDER_DISPATCHED_TRACKING_TOPIC = "dispatch.tracking";
    private static final UUID APPLICATION_ID = randomUUID ();

    private final KafkaTemplate<String, Object> kafkaProducer;
    private final StockServiceClient stockService;

    public void process (String key, OrderCreated payload) throws ExecutionException, InterruptedException {
        String available = stockService.checkAvailability (payload.getItem ());

        if (Boolean.TRUE.equals (Boolean.valueOf (available))) {
            OrderDispatched orderDispatched = OrderDispatched.builder ()
                    .orderId (payload.getOrderId ())
                    .processedById (APPLICATION_ID)
                    .notes ("Dispatched: " + payload.getItem ())
                    .build ();
            kafkaProducer.send (ORDER_DISPATCHED_TOPIC, key, orderDispatched).get ();
            log.info ("Sent messages: orderId: {} - processedById: {}, key - {}", payload.getOrderId (), APPLICATION_ID, key);

            DispatchPreparingEvent event = DispatchPreparingEvent.builder ()
                    .orderId (payload.getOrderId ())
                    .build ();
            kafkaProducer.send (ORDER_DISPATCHED_TRACKING_TOPIC, key, event).get ();

            processCompletedEvent (key, payload);
        } else {
            log.info ("Item not available: {}", payload.getItem ());
        }

    }

    private void processCompletedEvent (final String key, final OrderCreated payload) throws InterruptedException, ExecutionException {
        log.info ("Some other business logic here...");
        Thread.sleep (150);
        DispatchCompletedEvent event = DispatchCompletedEvent.builder ()
                .orderId (payload.getOrderId ())
                .date (LocalDate.now ().toString ())
                .build ();
        kafkaProducer.send (ORDER_DISPATCHED_TRACKING_TOPIC, key, event).get ();
    }


}
