package com.saga.choreography.common.event;

import java.math.BigDecimal;

public record StockReservedEvent(BaseEvent base, String sku, int quantity, BigDecimal orderTotal) implements SagaEvent {
}
