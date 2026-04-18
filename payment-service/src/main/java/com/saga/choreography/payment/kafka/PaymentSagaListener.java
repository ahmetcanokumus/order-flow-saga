package com.saga.choreography.payment.kafka;

import com.saga.choreography.common.SagaTopics;
import com.saga.choreography.common.event.SagaEvent;
import com.saga.choreography.common.event.StockReservedEvent;
import com.saga.choreography.common.tracing.MdcTraceBridge;
import com.saga.choreography.common.tracing.TraceKafkaHeaders;
import com.saga.choreography.payment.service.IdempotencyService;
import com.saga.choreography.payment.service.PaymentDecisionService;
import com.saga.choreography.payment.service.PaymentOutboxWriter;
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
public class PaymentSagaListener {

    private final PaymentDecisionService paymentDecisionService;
    private final PaymentOutboxWriter paymentOutboxWriter;
    private final IdempotencyService idempotencyService;

    @KafkaListener(
            topics = SagaTopics.STOCK_RESERVED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "stockReservedKafkaListenerContainerFactory"
    )
    @Transactional
    public void onStockReserved(
            @Payload StockReservedEvent event,
            @Header(name = TraceKafkaHeaders.TRACE_ID, required = false) byte[] traceIdHeader
    ) {
        applyMdc(event.traceId(), traceIdHeader);
        try {
            if (idempotencyService.isDuplicate(event.orderId(), event.eventId())) {
                log.info("duplicate event skipped kind=StockReserved orderId={} eventId={}", event.orderId(), event.eventId());
                return;
            }
            SagaEvent outcome = paymentDecisionService.onStockReserved(event);
            paymentOutboxWriter.enqueue(outcome);
            idempotencyService.recordProcessed(event.orderId(), event.eventId(), "StockReserved");
            log.info("recorded payment outcome {} for orderId={}", outcome.getClass().getSimpleName(), event.orderId());
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
