package dev.tech.dispatch.integration;

import dev.tech.dispatch.config.KafkaTestListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static dev.tech.dispatch.integration.WiremockUtils.stubWiremock;
import static dev.tech.dispatch.service.DispatcherService.ORDER_CREATED_TOPIC;
import static dev.tech.dispatch.util.TestEventData.buildOrderCreated;
import static java.util.UUID.randomUUID;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

@SpringBootTest
@AutoConfigureWireMock (port = 0)
@EmbeddedKafka (controlledShutdown = true)
@ActiveProfiles ("test")
@Slf4j
class DLTdispatchTopicIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private KafkaListenerEndpointRegistry registry;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private KafkaTestListener kafkaTestListener;

    @BeforeEach
    public void setUp () {
        kafkaTestListener.orderDltTopic.set (0);
        WiremockUtils.reset ();

        registry.getListenerContainers ().forEach (container ->
                ContainerTestUtils.waitForAssignment (container,
                        container.getContainerProperties ().getTopics ().length * embeddedKafkaBroker.getPartitionsPerTopic ()));
    }

    @Test
    public void testDLTTopic () throws Exception {
        stubWiremock ("/api/stock?item=test-item3", 404, "Bad request");
        String key = randomUUID ().toString ();
        kafkaTemplate.send (ORDER_CREATED_TOPIC, key, buildOrderCreated (randomUUID (), "test-item3"));
        await ().atMost (5, TimeUnit.SECONDS).pollDelay (100, TimeUnit.MILLISECONDS)
                .until (kafkaTestListener.orderDltTopic::get, equalTo (1));

    }

}
