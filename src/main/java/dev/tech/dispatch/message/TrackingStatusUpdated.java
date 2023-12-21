package dev.tech.dispatch.message;

import dev.tech.dispatch.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingStatusUpdated {
    private UUID orderId;
    private Status status;
}
