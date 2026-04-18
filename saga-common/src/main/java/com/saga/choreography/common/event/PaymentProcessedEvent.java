package com.saga.choreography.common.event;

import java.math.BigDecimal;

public record PaymentProcessedEvent(BaseEvent base, String paymentReference, BigDecimal amount) implements SagaEvent {
}
