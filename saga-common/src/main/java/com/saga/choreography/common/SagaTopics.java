package com.saga.choreography.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SagaTopics {

    public static final String ORDER_CREATED = "saga.order.created";
    public static final String STOCK_RESERVED = "saga.stock.reserved";
    public static final String STOCK_LIMIT_EXCEEDED = "saga.stock.limit-exceeded";
    public static final String PAYMENT_PROCESSED = "saga.payment.processed";
    public static final String PAYMENT_FAILED = "saga.payment.failed";
}
