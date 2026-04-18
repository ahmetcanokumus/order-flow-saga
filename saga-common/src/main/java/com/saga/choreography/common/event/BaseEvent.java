package com.saga.choreography.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

/**
 * Metadata present on every saga message for correlation and idempotency.
 */
public record BaseEvent(
        UUID orderId,
        UUID eventId,
        String traceId,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
}
