package com.saga.choreography.common.event;

import java.util.UUID;

/**
 * Sealed contract for all choreography payloads (enables exhaustive switch pattern matching).
 */
public sealed interface SagaEvent permits
        OrderCreatedEvent,
        StockReservedEvent,
        StockLimitExceededEvent,
        PaymentProcessedEvent,
        PaymentFailedEvent {

    BaseEvent base();

    default UUID orderId() {
        return base().orderId();
    }

    default UUID eventId() {
        return base().eventId();
    }

    default String traceId() {
        return base().traceId();
    }
}
