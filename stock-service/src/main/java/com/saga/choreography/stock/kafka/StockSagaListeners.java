package com.saga.choreography.stock.kafka;

import com.saga.choreography.common.SagaTopics;
import com.saga.choreography.common.event.OrderCreatedEvent;
import com.saga.choreography.common.event.PaymentFailedEvent;
import com.saga.choreography.common.event.PaymentProcessedEvent;
import com.saga.choreography.common.event.SagaEvent;
import com.saga.choreography.common.tracing.MdcTraceBridge;
import com.saga.choreography.common.tracing.TraceKafkaHeaders;
import com.saga.choreography.stock.saga.StockDispatch;
import com.saga.choreography.stock.service.IdempotencyService;
import com.saga.choreography.stock.service.StockOutboxWriter;
import com.saga.choreography.stock.service.StockSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockSagaListeners {

    private final StockSagaService stockSagaService;
    private final StockOutboxWriter stockOutboxWriter;
    private final IdempotencyService idempotencyService;

    @KafkaListener(
            topics = SagaTopics.ORDER_CREATED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    @Transactional
    public void onOrderCreated(
            @Payload OrderCreatedEvent event,
            @Header(name = TraceKafkaHeaders.TRACE_ID, required = false) byte[] traceIdHeader
    ) {
        applyMdc(event.traceId(), traceIdHeader);
        try {
            if (idempotencyService.isDuplicate(event.orderId(), event.eventId())) {
                log.info("duplicate event skipped kind=OrderCreated orderId={} eventId={}", event.orderId(), event.eventId());
                return;
            }
            StockDispatch dispatch = stockSagaService.onOrderCreated(event);
            SagaEvent outbound = switch (dispatch) {
                case StockDispatch.Reserved r -> r.outboundEvent();
                case StockDispatch.LimitExceeded l -> l.outboundEvent();
            };
            stockOutboxWriter.enqueue(outbound);
            idempotencyService.recordProcessed(event.orderId(), event.eventId(), "OrderCreated");
        } finally {
            MdcTraceBridge.clearTraceId();
        }
    }

    @KafkaListener(
            topics = SagaTopics.PAYMENT_PROCESSED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentProcessedKafkaListenerContainerFactory"
    )
    @Transactional
    public void onPaymentProcessed(
            @Payload PaymentProcessedEvent event,
            @Header(name = TraceKafkaHeaders.TRACE_ID, required = false) byte[] traceIdHeader
    ) {
        applyMdc(event.traceId(), traceIdHeader);
        try {
            if (idempotencyService.isDuplicate(event.orderId(), event.eventId())) {
                log.info("duplicate event skipped kind=PaymentProcessed orderId={} eventId={}", event.orderId(), event.eventId());
                return;
            }
            stockSagaService.commitReservation(event);
            idempotencyService.recordProcessed(event.orderId(), event.eventId(), "PaymentProcessed");
            log.info("stock committed for orderId={}", event.orderId());
        } finally {
            MdcTraceBridge.clearTraceId();
        }
    }

    @KafkaListener(
            topics = SagaTopics.PAYMENT_FAILED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    @Transactional
    public void onPaymentFailed(
            @Payload PaymentFailedEvent event,
            @Header(name = TraceKafkaHeaders.TRACE_ID, required = false) byte[] traceIdHeader
    ) {
        applyMdc(event.traceId(), traceIdHeader);
        try {
            if (idempotencyService.isDuplicate(event.orderId(), event.eventId())) {
                log.info("duplicate event skipped kind=PaymentFailed orderId={} eventId={}", event.orderId(), event.eventId());
                return;
            }
            stockSagaService.releaseReservation(event);
            idempotencyService.recordProcessed(event.orderId(), event.eventId(), "PaymentFailed");
            log.info("stock released for orderId={}", event.orderId());
        } finally {
            MdcTraceBridge.clearTraceId();
        }
    }

    private static void applyMdc(String payloadTraceId, byte[] traceIdHeader) {
        String fromHeader = Optional.ofNullable(traceIdHeader)
                .map(h -> new String(h, StandardCharsets.UTF_8))
                .filter(s -> !s.isBlank())
                .orElse(null);
        if (fromHeader != null) {
            MdcTraceBridge.put(fromHeader);
        } else if (payloadTraceId != null) {
            MdcTraceBridge.put(payloadTraceId);
        }
    }
}
