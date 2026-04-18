package com.saga.choreography.stock.saga;

import com.saga.choreography.common.event.SagaEvent;
import com.saga.choreography.common.event.StockLimitExceededEvent;
import com.saga.choreography.common.event.StockReservedEvent;

/**
 * Result of attempting to reserve stock (exhaustive switch over sealed hierarchy).
 */
public sealed interface StockDispatch permits StockDispatch.Reserved, StockDispatch.LimitExceeded {

    SagaEvent outboundEvent();

    record Reserved(StockReservedEvent event) implements StockDispatch {
        @Override
        public SagaEvent outboundEvent() {
            return event;
        }
    }

    record LimitExceeded(StockLimitExceededEvent event) implements StockDispatch {
        @Override
        public SagaEvent outboundEvent() {
            return event;
        }
    }
}
