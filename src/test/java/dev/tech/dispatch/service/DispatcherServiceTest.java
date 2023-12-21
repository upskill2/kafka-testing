package dev.tech.dispatch.service;

import dev.tech.dispatch.message.DispatchPreparingEvent;
import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.message.OrderDispatched;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static dev.tech.dispatch.util.TestEventData.buildOrderCreated;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DispatcherServiceTest {

    private DispatcherService dispatcherService;
    private KafkaTemplate<String, Object> kafkaTemplateMock;

    @BeforeEach
    void setUp () {
        kafkaTemplateMock = mock (KafkaTemplate.class);
        dispatcherService = new DispatcherService (kafkaTemplateMock);
    }

    @Test
    void process_Success () throws ExecutionException, InterruptedException {
        when (kafkaTemplateMock.send (anyString (), any (OrderDispatched.class))).thenReturn (mock (CompletableFuture.class));
        when (kafkaTemplateMock.send (anyString (), any (DispatchPreparingEvent.class))).thenReturn (mock (CompletableFuture.class));

        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());
        dispatcherService.process (testEvent);

        verify (kafkaTemplateMock, times (1)).send (eq ("order.dispatched"), any (OrderDispatched.class));
        verify (kafkaTemplateMock, times (1)).send (eq ("dispatch.tracking"), any (DispatchPreparingEvent.class));
    }

    @Test
    void process_ThrowsException () throws ExecutionException, InterruptedException {
        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());
        doThrow (new RuntimeException ("Test Exception")).when (kafkaTemplateMock).send (eq ("order.dispatched"), any (OrderDispatched.class));

        Exception exception = assertThrows (RuntimeException.class, () -> dispatcherService.process (testEvent));
        verify (kafkaTemplateMock, times (1)).send (eq ("order.dispatched"), any (OrderDispatched.class));
        assertEquals ("Test Exception", exception.getMessage ());
    }

    @Test
    void process_ThrowsExceptionForDispatchPreparingEvent () throws ExecutionException, InterruptedException {
        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());
        when (kafkaTemplateMock.send (anyString (), any (OrderDispatched.class))).thenReturn (mock (CompletableFuture.class));
        doThrow (new RuntimeException ("Test Exception")).when (kafkaTemplateMock).send (eq ("dispatch.tracking"), any (DispatchPreparingEvent.class));

        Exception exception = assertThrows (RuntimeException.class, () -> dispatcherService.process (testEvent));
        verify (kafkaTemplateMock, times (1)).send (eq ("dispatch.tracking"), any (DispatchPreparingEvent.class));
        assertEquals ("Test Exception", exception.getMessage ());
    }

}