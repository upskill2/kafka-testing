package dev.tech.dispatch.handler;

import dev.tech.dispatch.exceptions.NonRetryableException;
import dev.tech.dispatch.exceptions.RetryableException;
import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.service.DispatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.ExecutionException;

import static dev.tech.dispatch.util.TestEventData.buildOrderCreated;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.equalTo;

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
    void listen_ThrowsNonRetryableException () throws ExecutionException, InterruptedException {
        String key = randomUUID ().toString ();
        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());

        doThrow (new RuntimeException ("Test Exception")).when (dispatcherServiceMock).process (key, testEvent);

        Exception exception = assertThrows (NonRetryableException.class, () -> orderCreatedHandler.listen (0, key, testEvent));
        assertThat (exception.getMessage (), equalTo ("java.lang.RuntimeException: Test Exception"));
        verify (dispatcherServiceMock, times (1)).process (key, testEvent);
    }

    @Test
    void listen_ThrowsRetryableException () throws ExecutionException, InterruptedException {
        String key = randomUUID ().toString ();
        OrderCreated testEvent = buildOrderCreated (randomUUID (), randomUUID ().toString ());

        doThrow (new RetryableException ("Test Exception_2")).when (dispatcherServiceMock).process (key, testEvent);

        Exception exception = assertThrows (RuntimeException.class, () -> orderCreatedHandler.listen (0, key, testEvent));
        assertThat (exception.getMessage (), equalTo ("Test Exception_2"));
        verify (dispatcherServiceMock, times (1)).process (key, testEvent);
    }

}