package dev.tech.dispatch.util;

import dev.tech.dispatch.message.DispatchCompletedEvent;
import dev.tech.dispatch.message.DispatchPreparingEvent;
import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.message.TrackingStatusUpdated;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public class TestEventData {
    public static OrderCreated buildOrderCreated (UUID orderId, String Item) {
        return OrderCreated.builder ()
                .orderId (orderId)
                .item (Item)
                .build ();
    }

    public static TrackingStatusUpdated buildTrackingStatusUpdatedEvent () {
        return TrackingStatusUpdated.builder ()
                .orderId (randomUUID ())
                .status (Status.PREPARING)
                .build ();
    }

    public static DispatchPreparingEvent buildDispatchPreparingEvent () {
        return DispatchPreparingEvent.builder ()
                .orderId (randomUUID ())
                .build ();
    }

    public static DispatchCompletedEvent buildDispatchCompletedEvent () {
        return DispatchCompletedEvent.builder ()
                .orderId (randomUUID ())
                .date (LocalDate.now ().toString ())
                .build ();
    }

}
