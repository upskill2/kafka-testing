package dev.tech.dispatch.config;

import dev.tech.dispatch.configs.DispatchConfiguration;
import dev.tech.dispatch.message.DispatchPreparingEvent;
import dev.tech.dispatch.message.OrderDispatched;
import dev.tech.dispatch.message.TrackingStatusUpdated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.util.AssertionErrors.assertNotEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@TestConfiguration
@Slf4j
//@Profile ("test")
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
    public void listenOrderDispatchedTopic (@Header (KafkaHeaders.RECEIVED_KEY) String key, @Payload OrderDispatched payload) {
        log.info ("KafkaTestListener received event: {} from topic: {} with key: {}", payload, ORDER_DISPATCHED_TOPIC, key);
        orderDispatchedTopic.incrementAndGet ();
        assertNotNull ("Dispatcher topic payload is not null", payload);
        assertNotNull ("Dispatcher key is not null", key);
    }

    @KafkaListener (
            groupId = "trackingServiceIntegrationTest",
            topics = ORDER_DISPATCHED_TRACKING_TOPIC
    )
    public void listenOrderDispatchedTrackingTopic (@Header (KafkaHeaders.RECEIVED_KEY) String key, @Payload DispatchPreparingEvent payload) {
        log.info ("KafkaTestListener received event: {} from topic: {} with key: {}", payload, ORDER_DISPATCHED_TOPIC, key);
        orderDispatchedTrackingTopic.incrementAndGet ();
        assertNotNull ("Dispatcher Tracking topic payload is not null", payload);
        assertNotNull ("Dispatcher Tracking key is not null", key);
    }

    @KafkaListener (
            groupId = "trackingServiceIntegrationTest",
            topics = TRACKING_STATUS_TOPIC
    )
    public void listenTrackingStatusTopic (@Header (KafkaHeaders.RECEIVED_KEY) String key, @Payload TrackingStatusUpdated payload) {
        log.info ("KafkaTestListener received event: {} from topic: {} with key: {}", payload, ORDER_DISPATCHED_TOPIC, key);
        trackingStatusTopic.incrementAndGet ();
        assertNotNull ("Tracking Status topic payload is not null", payload);
        assertNotNull ("Tracking Status key is not null", key);
    }

}
