package dev.tech.dispatch.integration;

import dev.tech.dispatch.configs.DispatchConfiguration;
import dev.tech.dispatch.config.KafkaTestListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
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

import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static dev.tech.dispatch.integration.WiremockUtils.stubWiremock;
import static dev.tech.dispatch.service.DispatcherService.ORDER_CREATED_TOPIC;
import static dev.tech.dispatch.util.TestEventData.buildOrderCreated;
import static java.util.UUID.randomUUID;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@Slf4j
@SpringBootTest (classes = {DispatchConfiguration.class})
@ActiveProfiles ("test")
@EmbeddedKafka (controlledShutdown = true)
@DirtiesContext (classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureWireMock (port = 0)
class OrderDispatchIntegrationTest {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private KafkaTestListener kafkaTestListener;
    @Autowired
    private KafkaListenerEndpointRegistry registry;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

 /*   @Configuration
   private static class TestConfig {
        @Bean
        public KafkaTestListener kafkaTestListener () {
            return new KafkaTestListener ();
        }
    }

    private static class KafkaTestListener {
        AtomicInteger orderDispatchedTopic = new AtomicInteger (0);
        AtomicInteger orderDispatchedTrackingTopic = new AtomicInteger (0);

        @KafkaListener (
                groupId = "kafkaIntegrationTest",
                topics = ORDER_DISPATCHED_TOPIC
        )
        void receiveDispatchedPreparing (@Payload OrderDispatched event) {
            log.info ("Received DispatchPreparingEvent: {}", event);
            orderDispatchedTopic.incrementAndGet ();
        }

        @KafkaListener (
                groupId = "kafkaIntegrationTest",
                topics = ORDER_DISPATCHED_TRACKING_TOPIC
        )
        void receiveDispatchedTracking (@Payload DispatchPreparingEvent event) {
            log.info ("Received Dispatch Tacking event: {}", event);
            orderDispatchedTrackingTopic.incrementAndGet ();
        }

    }*/

    @BeforeEach
    public void setUp () throws InterruptedException {
        kafkaTestListener.orderDispatchedTopic.set (0);
        kafkaTestListener.orderDispatchedTrackingTopic.set (0);

        WiremockUtils.reset ();

        registry.getListenerContainers ().forEach (container ->
                ContainerTestUtils.waitForAssignment (container,
                        container.getContainerProperties ().getTopics ().length * embeddedKafkaBroker.getPartitionsPerTopic ()));
    }

    @Test
    void testOrderDispatchFlow_200 () throws Exception {
        stubWiremock ("/api/stock?item=test-item", 200, "true");
        String key = randomUUID ().toString ();
        sendMessage (ORDER_CREATED_TOPIC, key, buildOrderCreated (randomUUID (), "test-item"));

        await ().atMost (5, TimeUnit.SECONDS).pollDelay (100, TimeUnit.MILLISECONDS)
                .until (kafkaTestListener.orderDispatchedTopic::get, equalTo (1));

        await ().atMost (1, TimeUnit.SECONDS).pollDelay (300, TimeUnit.MILLISECONDS)
                .until (kafkaTestListener.orderDispatchedTrackingTopic::get, equalTo (2));
    }

    @Test
    void testOrderDispatchFlow_503_Retrayable () throws Exception {
        stubWiremock ("/api/stock?item=test-item", 503, "Service Unavailable", "failOnce", STARTED, "succeedNextTime");
        stubWiremock ("/api/stock?item=test-item", 200, "true", "failOnce", "succeedNextTime", "succeedNextTime");
        String key = randomUUID ().toString ();
        sendMessage (ORDER_CREATED_TOPIC, key, buildOrderCreated (randomUUID (), "test-item"));

        await ().atMost (5, TimeUnit.SECONDS).pollDelay (100, TimeUnit.MILLISECONDS)
                .until (kafkaTestListener.orderDispatchedTopic::get, equalTo (1));

        await ().atMost (1, TimeUnit.SECONDS).pollDelay (100, TimeUnit.MILLISECONDS)
                .until (kafkaTestListener.orderDispatchedTrackingTopic::get, equalTo (2));
    }

    @Test
    void testOrderDispatchFlow_404_NonRetrayable () throws Exception {
        stubWiremock ("/api/stock?item=test-item", 404, "Bad Request");
        String key = randomUUID ().toString ();
        sendMessage (ORDER_CREATED_TOPIC, key, buildOrderCreated (randomUUID (), "test-item"));

        TimeUnit.SECONDS.sleep (3);
        assertThat (kafkaTestListener.orderDispatchedTopic.get (), equalTo (0));
        assertThat (kafkaTestListener.orderDispatchedTrackingTopic.get (), equalTo (0));
    }

    private void sendMessage (final String topic, String key, Object data) throws Exception {
        kafkaTemplate.send (MessageBuilder
                        .withPayload (data)
                        .setHeader (KafkaHeaders.TOPIC, topic)
                        .setHeader (KafkaHeaders.KEY, key)
                        .build ())
                .get ();
    }

}
