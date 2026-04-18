package com.saga.choreography.common.event;

public record StockLimitExceededEvent(BaseEvent base, String sku, int requestedQuantity) implements SagaEvent {
}
