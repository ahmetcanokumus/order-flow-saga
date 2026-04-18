package com.saga.choreography.common.event;

public record PaymentFailedEvent(BaseEvent base, String reasonCode, String detail) implements SagaEvent {
}
