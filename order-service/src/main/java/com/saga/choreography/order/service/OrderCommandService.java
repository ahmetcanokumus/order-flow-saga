package com.saga.choreography.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.choreography.common.SagaTopics;
import com.saga.choreography.common.event.BaseEvent;
import com.saga.choreography.common.event.OrderCreatedEvent;
import com.saga.choreography.common.tracing.MdcTraceBridge;
import com.saga.choreography.order.entity.OrderEntity;
import com.saga.choreography.order.entity.OrderStatus;
import com.saga.choreography.order.entity.OutboxMessageEntity;
import com.saga.choreography.order.repository.OrderRepository;
import com.saga.choreography.order.repository.OutboxMessageRepository;
import com.saga.choreography.order.tracing.TraceIdSupplier;
import com.saga.choreography.order.web.CreateOrderRequest;
import com.saga.choreography.order.web.CreateOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final TraceIdSupplier traceIdSupplier;
    private final ObjectMapper objectMapper;

    @Transactional
    public CreateOrderResponse createPendingOrder(CreateOrderRequest request) {
        String traceId = traceIdSupplier.currentOrNew();
        MdcTraceBridge.put(traceId);
        try {
            UUID orderId = UUID.randomUUID();
            UUID eventId = UUID.randomUUID();
            Instant now = Instant.now();

            OrderEntity order = new OrderEntity();
            order.setId(orderId);
            order.setCustomerId(request.customerId());
            order.setTotalAmount(request.totalAmount());
            order.setSku(request.sku());
            order.setQuantity(request.quantity());
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(now);
            orderRepository.save(order);

            BaseEvent base = new BaseEvent(orderId, eventId, traceId, now);
            OrderCreatedEvent event = new OrderCreatedEvent(
                    base,
                    request.customerId(),
                    request.totalAmount(),
                    request.sku(),
                    request.quantity()
            );

            OutboxMessageEntity outbox = new OutboxMessageEntity();
            outbox.setOrderId(orderId);
            outbox.setTopic(SagaTopics.ORDER_CREATED);
            outbox.setPayloadType(OrderCreatedEvent.class.getName());
            outbox.setPayloadJson(writeJson(event));
            outbox.setTraceId(traceId);
            outbox.setCreatedAt(now);
            outboxMessageRepository.save(outbox);

            return new CreateOrderResponse(orderId, traceId);
        } finally {
            MdcTraceBridge.clearTraceId();
        }
    }

    private String writeJson(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize OrderCreatedEvent", e);
        }
    }
}
