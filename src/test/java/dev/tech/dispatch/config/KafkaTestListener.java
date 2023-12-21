package dev.tech.dispatch.config;

import dev.tech.dispatch.configs.DispatchConfiguration;
import dev.tech.dispatch.message.DispatchCompletedEvent;
import dev.tech.dispatch.message.DispatchPreparingEvent;
import dev.tech.dispatch.message.OrderDispatched;
import dev.tech.dispatch.message.TrackingStatusUpdated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.tech.dispatch.service.DispatcherService.ORDER_DISPATCHED_TOPIC;
import static dev.tech.dispatch.service.DispatcherService.ORDER_DISPATCHED_TRACKING_TOPIC;
import static dev.tech.dispatch.service.TrackingService.TRACKING_STATUS_TOPIC;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@TestConfiguration
@Slf4j
//@Profile ("test")
@KafkaListener (
        groupId = "trackingServiceIntegrationTest",
        topics = {ORDER_DISPATCHED_TOPIC, ORDER_DISPATCHED_TRACKING_TOPIC, TRACKING_STATUS_TOPIC}
)
public class KafkaTestListener {

    public AtomicInteger orderDispatchedTopic = new AtomicInteger (0);
    public AtomicInteger orderDispatchedTrackingTopic = new AtomicInteger (0);
    public AtomicInteger trackingStatusTopic = new AtomicInteger (0);

    @KafkaHandler
    public void listenOrderDispatchedTopic (@Header (KafkaHeaders.RECEIVED_KEY) String key,
                                            @Header (KafkaHeaders.RECEIVED_TOPIC) String topic,
                                            @Payload OrderDispatched payload) {
        log.info ("KafkaTestListener received event: {} from topic: {} with key: {}", payload, topic, key);
        orderDispatchedTopic.incrementAndGet ();
        assertNotNull ("Dispatcher topic payload is not null", payload);
        assertNotNull ("Dispatcher key is not null", key);
    }

    /*    @KafkaListener (
                groupId = "trackingServiceIntegrationTest",
                topics = ORDER_DISPATCHED_TRACKING_TOPIC
        )*/
    @KafkaHandler
    public void listenOrderDispatchedTrackingTopic (@Header (KafkaHeaders.RECEIVED_KEY) String key,
                                                    @Header (KafkaHeaders.RECEIVED_TOPIC) String topic,
                                                    @Payload DispatchPreparingEvent payload) {
        log.info ("KafkaTestListener received event: {} from topic: {} with key: {}", payload, topic, key);
        orderDispatchedTrackingTopic.incrementAndGet ();
        assertNotNull ("Dispatcher Tracking topic payload is not null", payload);
        assertNotNull ("Dispatcher Tracking key is not null", key);
    }

    @KafkaHandler
    public void listenOrderCompletedEventTopic (@Header (KafkaHeaders.RECEIVED_KEY) String key,
                                                @Header (KafkaHeaders.RECEIVED_TOPIC) String topic,
                                                @Payload DispatchCompletedEvent payload) {
        log.info ("KafkaTestListener received event: {} from topic: {} with key: {}", payload, topic, key);
        orderDispatchedTrackingTopic.incrementAndGet ();
        assertNotNull ("Dispatcher Tracking topic payload is not null", payload);
        assertNotNull ("Dispatcher Tracking key is not null", key);
    }

    /*    @KafkaListener (
                groupId = "trackingServiceIntegrationTest",
                topics = TRACKING_STATUS_TOPIC
        )*/
    @KafkaHandler
    public void listenTrackingStatusTopic (@Header (KafkaHeaders.RECEIVED_KEY) String key,
                                           @Header (KafkaHeaders.RECEIVED_TOPIC) String topic,
                                           @Payload TrackingStatusUpdated payload) {
        log.info ("KafkaTestListener received event: {} from topic: {} with key: {}", payload, topic, key);
        trackingStatusTopic.incrementAndGet ();
        assertNotNull ("Tracking Status topic payload is not null", payload);
        assertNotNull ("Tracking Status key is not null", key);
    }

}
