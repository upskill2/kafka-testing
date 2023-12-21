package dev.tech.dispatch.handler;

import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.service.DispatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static dev.tech.dispatch.util.TestEventData.buildOrderCreated;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;

class OrderCreatedHandlerTest {

    private OrderCreatedHandler orderCreatedHandler;
    private DispatcherService dispatcherServiceMock;

    @BeforeEach
    void setUp () {

        dispatcherServiceMock = mock (DispatcherService.class);
        orderCreatedHandler = new OrderCreatedHandler (dispatcherServiceMock);
    }

    @Test
    void listen_Success () throws ExecutionException, InterruptedException {
        String key = randomUUID ().toString ();
        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());
        orderCreatedHandler.listen (0, key, testEvent);
        verify (dispatcherServiceMock, times (1)).process (key, testEvent);
    }

    @Test
    void listen_ThrowsException () throws ExecutionException, InterruptedException {
        String key = randomUUID ().toString ();
        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());

        doThrow (new RuntimeException ("Test Exception")).when (dispatcherServiceMock).process (key, testEvent);

        orderCreatedHandler.listen (0, key, testEvent);
        verify (dispatcherServiceMock, times (1)).process (key, testEvent);
    }

}