package dev.tech.dispatch.service;

import dev.tech.dispatch.message.TrackingStatusUpdated;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static dev.tech.dispatch.service.TrackingService.TRACKING_STATUS_TOPIC;
import static dev.tech.dispatch.util.TestEventData.*;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackingServiceTest {

    private KafkaTemplate<String, Object> kafkaTemplateMock;
    private TrackingService trackingService;

    @BeforeEach
    void setUp () {
        kafkaTemplateMock = mock (KafkaTemplate.class);
        trackingService = new TrackingService (kafkaTemplateMock);
    }

    @Test
    void listen_PreparingEvent () throws ExecutionException, InterruptedException {
        when (kafkaTemplateMock.send (anyString (), anyString (), any (TrackingStatusUpdated.class))).thenReturn (mock (CompletableFuture.class));

        trackingService.listen (randomUUID ().toString (), 0, buildDispatchPreparingEvent ());
        verify (kafkaTemplateMock, times (1)).send (eq (TRACKING_STATUS_TOPIC), anyString (), any (TrackingStatusUpdated.class));
    }

    @Test
    void listen_CompletedEvent () throws ExecutionException, InterruptedException {
        when (kafkaTemplateMock.send (anyString (), anyString (), any (TrackingStatusUpdated.class))).thenReturn (mock (CompletableFuture.class));

        trackingService.listen (randomUUID ().toString (), 0, buildDispatchCompletedEvent ());
        verify (kafkaTemplateMock, times (1)).send (eq (TRACKING_STATUS_TOPIC), anyString (), any (TrackingStatusUpdated.class));
    }

    @Test
    void listen_throwException_WhenSendingPreparingEvent () {
        when (kafkaTemplateMock.send (anyString (), anyString (), any (TrackingStatusUpdated.class))).thenThrow (new RuntimeException ("Test Exception"));
        Exception exception = assertThrows (RuntimeException.class, () -> trackingService.listen (randomUUID ().toString (), 0, buildDispatchPreparingEvent ()));
        verify (kafkaTemplateMock, times (1)).send (eq (TRACKING_STATUS_TOPIC), anyString (), any (TrackingStatusUpdated.class));
        assertEquals ("Test Exception", exception.getMessage ());
    }

    @Test
    void listen_throwException_WhenSendingCompletedEvent () {
        when (kafkaTemplateMock.send (anyString (), anyString (), any (TrackingStatusUpdated.class))).thenThrow (new RuntimeException ("Test Exception_2"));
        Exception exception = assertThrows (RuntimeException.class, () -> trackingService.listen (randomUUID ().toString (), 0, buildDispatchCompletedEvent ()));
        verify (kafkaTemplateMock, times (1)).send (eq (TRACKING_STATUS_TOPIC), anyString (), any (TrackingStatusUpdated.class));
        assertEquals ("Test Exception_2", exception.getMessage ());
    }

}