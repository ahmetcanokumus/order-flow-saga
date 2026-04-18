package com.saga.choreography.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.choreography.common.tracing.MdcTraceBridge;
import com.saga.choreography.common.tracing.TraceKafkaHeaders;
import com.saga.choreography.order.entity.OutboxMessageEntity;
import com.saga.choreography.order.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxDispatchScheduler {

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${saga.outbox.poll-interval-ms:500}")
    public void tick() {
        var pending = outboxMessageRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxMessageEntity row : pending) {
            try {
                publishRow(row);
            } catch (Exception e) {
                log.error("Outbox publish failed for id={} orderId={}", row.getId(), row.getOrderId(), e);
            }
        }
    }

    private void publishRow(OutboxMessageEntity row)
            throws ClassNotFoundException, JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        Class<?> payloadClass = Class.forName(row.getPayloadType(), true, Thread.currentThread().getContextClassLoader());
        Object payload = objectMapper.readValue(row.getPayloadJson(), payloadClass);

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                row.getTopic(),
                row.getOrderId().toString(),
                payload
        );
        record.headers().add(TraceKafkaHeaders.TRACE_ID, row.getTraceId().getBytes(StandardCharsets.UTF_8));

        MdcTraceBridge.put(row.getTraceId());
        try {
            kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);
            row.setPublishedAt(Instant.now());
            outboxMessageRepository.save(row);
            log.info("Published outbox id={} topic={}", row.getId(), row.getTopic());
        } finally {
            MdcTraceBridge.clearTraceId();
        }
    }
}
