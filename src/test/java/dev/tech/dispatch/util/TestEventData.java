package dev.tech.dispatch.util;

import dev.tech.dispatch.message.DispatchPreparingEvent;
import dev.tech.dispatch.message.OrderCreated;
import dev.tech.dispatch.message.TrackingStatusUpdated;

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

}
