package dev.tech.dispatch.service;

import dev.tech.dispatch.message.DispatchCompletedEvent;
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
        String key = randomUUID ().toString ();
        when (kafkaTemplateMock.send (anyString (), anyString (), any (OrderDispatched.class))).thenReturn (mock (CompletableFuture.class));
        when (kafkaTemplateMock.send (anyString (), anyString (), any (DispatchPreparingEvent.class))).thenReturn (mock (CompletableFuture.class));
        when (kafkaTemplateMock.send (anyString (), anyString (), any (DispatchCompletedEvent.class))).thenReturn (mock (CompletableFuture.class));

        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());
        dispatcherService.process (key, testEvent);

        verify (kafkaTemplateMock, times (1)).send (eq ("order.dispatched"), eq (key), any (OrderDispatched.class));
        verify (kafkaTemplateMock, times (1)).send (eq ("dispatch.tracking"), eq (key), any (DispatchPreparingEvent.class));
        verify (kafkaTemplateMock, times (1)).send (eq ("dispatch.tracking"), eq (key), any (DispatchCompletedEvent.class));
    }

    @Test
    void process_ThrowsException () throws ExecutionException, InterruptedException {
        String key = randomUUID ().toString ();
        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());
        doThrow (new RuntimeException ("Test Exception")).when (kafkaTemplateMock).send (eq ("order.dispatched"),anyString (), any (OrderDispatched.class));

        Exception exception = assertThrows (RuntimeException.class, () -> dispatcherService.process (key, testEvent));
        verify (kafkaTemplateMock, times (1)).send (eq ("order.dispatched"), eq (key), any (OrderDispatched.class));
        assertEquals ("Test Exception", exception.getMessage ());
    }

    @Test
    void process_ThrowsExceptionForDispatchPreparingEvent () throws ExecutionException, InterruptedException {
        String key = randomUUID ().toString ();
        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());
        when (kafkaTemplateMock.send (anyString (), anyString (), any (OrderDispatched.class))).thenReturn (mock (CompletableFuture.class));
        doThrow (new RuntimeException ("Test Exception")).when (kafkaTemplateMock).send (eq ("dispatch.tracking"), eq (key), any (DispatchPreparingEvent.class));

        Exception exception = assertThrows (RuntimeException.class, () -> dispatcherService.process (key, testEvent));
        verify (kafkaTemplateMock, times (1)).send (eq ("dispatch.tracking"), eq (key), any (DispatchPreparingEvent.class));
        assertEquals ("Test Exception", exception.getMessage ());
    }
    @Test
    void process_ThrowsExceptionForDispatchCompletedEvent () throws ExecutionException, InterruptedException {
        String key = randomUUID ().toString ();
        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());
        when (kafkaTemplateMock.send (anyString (), anyString (), any (OrderDispatched.class))).thenReturn (mock (CompletableFuture.class));
        when(kafkaTemplateMock.send(eq ("dispatch.tracking"), eq (key), any (DispatchPreparingEvent.class))).thenReturn (mock (CompletableFuture.class));
        doThrow (new RuntimeException ("Test Exception_2")).when (kafkaTemplateMock).send (eq ("dispatch.tracking"), eq (key),
                any (DispatchCompletedEvent.class));

        Exception exception = assertThrows (RuntimeException.class, () -> dispatcherService.process (key, testEvent));
        verify (kafkaTemplateMock, times (1)).send (eq ("dispatch.tracking"), eq (key), any (DispatchCompletedEvent.class));
        assertEquals ("Test Exception_2", exception.getMessage ());
    }

}