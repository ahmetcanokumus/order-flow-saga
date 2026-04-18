package com.saga.choreography.order.kafka;

import com.saga.choreography.common.SagaTopics;
import com.saga.choreography.common.event.PaymentFailedEvent;
import com.saga.choreography.common.event.PaymentProcessedEvent;
import com.saga.choreography.common.tracing.MdcTraceBridge;
import com.saga.choreography.common.tracing.TraceKafkaHeaders;
import com.saga.choreography.order.entity.OrderStatus;
import com.saga.choreography.order.repository.OrderRepository;
import com.saga.choreography.order.service.IdempotencyService;
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
public class OrderSagaListeners {

    private final OrderRepository orderRepository;
    private final IdempotencyService idempotencyService;

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
            var orderOpt = orderRepository.findById(event.orderId());
            if (orderOpt.isEmpty()) {
                log.warn("order not found for approval orderId={}", event.orderId());
                return;
            }
            var order = orderOpt.get();
            order.setStatus(OrderStatus.APPROVED);
            orderRepository.save(order);
            log.info("order approved orderId={}", event.orderId());
            idempotencyService.recordProcessed(event.orderId(), event.eventId(), "PaymentProcessed");
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
            var orderOpt = orderRepository.findById(event.orderId());
            if (orderOpt.isEmpty()) {
                log.warn("order not found for cancellation orderId={}", event.orderId());
                return;
            }
            var order = orderOpt.get();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("order cancelled orderId={}", event.orderId());
            idempotencyService.recordProcessed(event.orderId(), event.eventId(), "PaymentFailed");
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
