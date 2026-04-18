package com.saga.choreography.payment.service;

import com.saga.choreography.payment.entity.ProcessedEventEntity;
import com.saga.choreography.payment.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public boolean isDuplicate(UUID orderId, UUID messageEventId) {
        return processedEventRepository.existsByOrderIdAndMessageEventId(orderId, messageEventId);
    }

    public void recordProcessed(UUID orderId, UUID messageEventId, String eventType) {
        ProcessedEventEntity row = new ProcessedEventEntity();
        row.setOrderId(orderId);
        row.setMessageEventId(messageEventId);
        row.setEventType(eventType);
        row.setProcessedAt(Instant.now());
        processedEventRepository.save(row);
    }
}
