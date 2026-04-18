package com.saga.choreography.payment.service;

import com.saga.choreography.common.event.BaseEvent;
import com.saga.choreography.common.event.PaymentFailedEvent;
import com.saga.choreography.common.event.PaymentProcessedEvent;
import com.saga.choreography.common.event.SagaEvent;
import com.saga.choreography.common.event.StockReservedEvent;
import com.saga.choreography.payment.config.PaymentSagaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentDecisionService {

    private final PaymentSagaProperties properties;

    /**
     * Pattern matching on configuration-driven outcomes (extend with instanceof/switch on event types as rules grow).
     */
    public SagaEvent onStockReserved(StockReservedEvent event) {
        BaseEvent nextBase = new BaseEvent(
                event.orderId(),
                UUID.randomUUID(),
                event.traceId(),
                Instant.now()
        );
        if (properties.isSimulateFailure()) {
            return new PaymentFailedEvent(nextBase, "DECLINED", "Simulated payment failure");
        }
        String reference = "PAY-" + UUID.randomUUID();
        return new PaymentProcessedEvent(nextBase, reference, event.orderTotal());
    }
}
