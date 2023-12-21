package dev.tech.dispatch.service;

import dev.tech.dispatch.message.DispatchPreparingEvent;
import dev.tech.dispatch.message.TrackingStatusUpdated;
import dev.tech.dispatch.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static dev.tech.dispatch.service.DispatcherService.ORDER_DISPATCHED_TRACKING_TOPIC;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {
    public static final String TRACKING_STATUS_TOPIC = "tracking.status";

    private final KafkaTemplate<String, Object> kafkaProducer;

    @KafkaListener (
            id = "trackingServiceClient",
            topics = ORDER_DISPATCHED_TRACKING_TOPIC,
            groupId = "tracking.service.consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen (DispatchPreparingEvent payload) throws ExecutionException, InterruptedException {
        log.info ("TrackingService received event: {} from topic: {}", payload, ORDER_DISPATCHED_TRACKING_TOPIC);
        emit (payload);
    }

    public void emit (DispatchPreparingEvent payload) throws ExecutionException, InterruptedException {
        TrackingStatusUpdated updatedEvent = TrackingStatusUpdated.builder ()
                .orderId (payload.getOrderId ())
                .status (Status.PREPARING)
                .build ();

        kafkaProducer.send (TRACKING_STATUS_TOPIC, updatedEvent).get ();
        log.info ("TrackingService sent event: {}  status to topic: {}", updatedEvent, TRACKING_STATUS_TOPIC);
    }

}
