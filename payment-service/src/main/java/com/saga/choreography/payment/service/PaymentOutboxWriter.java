package com.saga.choreography.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.choreography.common.event.SagaEvent;
import com.saga.choreography.common.event.SagaEventRouter;
import com.saga.choreography.payment.entity.OutboxMessageEntity;
import com.saga.choreography.payment.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentOutboxWriter {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public void enqueue(SagaEvent event) {
        try {
            OutboxMessageEntity row = new OutboxMessageEntity();
            row.setOrderId(event.orderId());
            row.setTopic(SagaEventRouter.topicFor(event));
            row.setPayloadType(event.getClass().getName());
            row.setPayloadJson(objectMapper.writeValueAsString(event));
            row.setTraceId(event.traceId());
            row.setCreatedAt(Instant.now());
            outboxMessageRepository.save(row);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize saga event " + event.getClass().getName(), e);
        }
    }
}
