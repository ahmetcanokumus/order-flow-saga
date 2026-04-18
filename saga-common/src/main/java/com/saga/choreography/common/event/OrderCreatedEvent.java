package com.saga.choreography.common.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        BaseEvent base,
        String customerId,
        BigDecimal totalAmount,
        String sku,
        int quantity
) implements SagaEvent {
}
