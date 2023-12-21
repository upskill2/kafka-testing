package dev.tech.dispatch.integration;

import dev.tech.dispatch.configs.DispatchConfiguration;
import dev.tech.dispatch.config.KafkaTestListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static dev.tech.dispatch.service.DispatcherService.ORDER_CREATED_TOPIC;
import static dev.tech.dispatch.util.TestEventData.buildOrderCreated;
import static java.util.UUID.randomUUID;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest (classes = {DispatchConfiguration.class})
@ActiveProfiles ("test")
@EmbeddedKafka (controlledShutdown = true)
@DirtiesContext (classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class TrackingServiceIntegrationTest {
/*
   // private static final String ORDER_CREATED_TOPIC = "order.created";
    private static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private static final String ORDER_DISPATCHED_TRACKING_TOPIC = "dispatch.tracking";
    private static final String TRACKING_STATUS_TOPIC = "tracking.status";*/

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private KafkaTestListener kafkaTestListener;
    @Autowired
    private KafkaListenerEndpointRegistry registry;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;



/*  @Configuration
   public static class TestConfig {
        @Bean
        public KafkaTestListener kafkaTestListener () {
            return new KafkaTestListener ();
        }
    }


    private static class KafkaTestListener {
        AtomicInteger orderDispatchedTopic = new AtomicInteger (0);
        AtomicInteger orderDispatchedTrackingTopic = new AtomicInteger (0);
        AtomicInteger trackingStatusTopic = new AtomicInteger (0);

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

    }*/

    @BeforeEach
    void setUp () {
       kafkaTestListener.orderDispatchedTopic.set (0);
       kafkaTestListener.orderDispatchedTrackingTopic.set (0);
       kafkaTestListener.trackingStatusTopic.set (0);

        registry.getListenerContainers ().forEach (container ->
                ContainerTestUtils.waitForAssignment (container,
                        container.getContainerProperties ().getTopics ().length * embeddedKafkaBroker.getPartitionsPerTopic ()));
    }

    @Test
    void testTrackingService () {
        kafkaTemplate.send (MessageBuilder
                .withPayload (buildOrderCreated (randomUUID (), "test-item2"))
                .setHeader (KafkaHeaders.TOPIC, ORDER_CREATED_TOPIC)
                        .setHeader (KafkaHeaders.KEY, randomUUID ().toString ())
                .build ());

        await ().atMost (5, TimeUnit.SECONDS).pollDelay (100, TimeUnit.MILLISECONDS)
                .until (kafkaTestListener.orderDispatchedTopic::get, equalTo (1));

        await ().atMost (1, TimeUnit.SECONDS).pollDelay (100, TimeUnit.MILLISECONDS)
                .until (kafkaTestListener.orderDispatchedTrackingTopic::get, equalTo (1));

        await ().atMost (1, TimeUnit.SECONDS).pollDelay (100, TimeUnit.MILLISECONDS)
                .until (kafkaTestListener.trackingStatusTopic::get, equalTo (2));
    }

}
