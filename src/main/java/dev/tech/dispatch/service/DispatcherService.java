package dev.tech.dispatch.service;

import dev.tech.dispatch.message.DispatchPreparingEvent;
import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.message.OrderDispatched;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class DispatcherService {

    private final KafkaTemplate<String, Object> kafkaProducer;
    private static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    public static final String ORDER_DISPATCHED_TRACKING_TOPIC = "dispatch.tracking";

    public void process (OrderCreated payload) throws ExecutionException, InterruptedException {
        OrderDispatched orderDispatched = OrderDispatched.builder ()
                .orderId (payload.getOrderId ())
                .build ();
       kafkaProducer.send (ORDER_DISPATCHED_TOPIC, orderDispatched).get ();

        DispatchPreparingEvent event = DispatchPreparingEvent.builder ()
                .orderId (payload.getOrderId ())
                .build ();
        kafkaProducer.send (ORDER_DISPATCHED_TRACKING_TOPIC, event).get ();

    }


}
