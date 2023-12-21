package dev.tech.dispatch.service;

import dev.tech.dispatch.configs.DispatchConfiguration;
import dev.tech.dispatch.message.DispatchPreparingEvent;
import dev.tech.dispatch.message.OrderDispatched;
import dev.tech.dispatch.message.TrackingStatusUpdated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Slf4j
@Profile ("test")
//@Component
public class KafkaTestListener {

    private static final String ORDER_CREATED_TOPIC = "order.created";
    private static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private static final String ORDER_DISPATCHED_TRACKING_TOPIC = "dispatch.tracking";
    private static final String TRACKING_STATUS_TOPIC = "tracking.status";


   public AtomicInteger orderDispatchedTopic = new AtomicInteger (0);
    public AtomicInteger orderDispatchedTrackingTopic = new AtomicInteger (0);
    public AtomicInteger trackingStatusTopic = new AtomicInteger (0);

    @KafkaListener (
            groupId = "trackingServiceIntegrationTest",
            topics = ORDER_DISPATCHED_TOPIC
    )
    public void listenOrderDispatchedTopic (@Payload OrderDispatched payload) {
        log.info ("KafkaTestListener received event: {} from topic: {}", payload, ORDER_DISPATCHED_TOPIC);
        orderDispatchedTopic.incrementAndGet ();
    }

    @KafkaListener (
            groupId = "trackingServiceIntegrationTest",
            topics = ORDER_DISPATCHED_TRACKING_TOPIC
    )
    public void listenOrderDispatchedTrackingTopic (@Payload DispatchPreparingEvent payload) {
        log.info ("KafkaTestListener received event: {} from topic: {}", payload, ORDER_DISPATCHED_TOPIC);
        orderDispatchedTrackingTopic.incrementAndGet ();
    }

    @KafkaListener (
            groupId = "trackingServiceIntegrationTest",
            topics = TRACKING_STATUS_TOPIC
    )
    public void listenTrackingStatusTopic (@Payload TrackingStatusUpdated payload) {
        log.info ("KafkaTestListener received event: {} from topic: {}", payload, ORDER_DISPATCHED_TOPIC);
        trackingStatusTopic.incrementAndGet ();
    }

}
