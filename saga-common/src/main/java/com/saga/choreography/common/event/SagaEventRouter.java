package com.saga.choreography.common.event;

import com.saga.choreography.common.SagaTopics;
import lombok.experimental.UtilityClass;

/**
 * Exhaustive routing of events to Kafka topics (pattern matching on sealed interface).
 */
@UtilityClass
public class SagaEventRouter {

    public static String topicFor(SagaEvent event) {
        return switch (event) {
            case OrderCreatedEvent ignored -> SagaTopics.ORDER_CREATED;
            case StockReservedEvent ignored -> SagaTopics.STOCK_RESERVED;
            case StockLimitExceededEvent ignored -> SagaTopics.STOCK_LIMIT_EXCEEDED;
            case PaymentProcessedEvent ignored -> SagaTopics.PAYMENT_PROCESSED;
            case PaymentFailedEvent ignored -> SagaTopics.PAYMENT_FAILED;
        };
    }
}
