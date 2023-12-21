package dev.tech.dispatch.message;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class OrderDispatched {

    private UUID orderId;
}
