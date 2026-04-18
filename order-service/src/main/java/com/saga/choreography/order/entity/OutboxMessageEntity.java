package com.saga.choreography.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
@Getter
@Setter
public class OutboxMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID orderId;

    @Column(nullable = false, length = 256)
    private String topic;

    @Column(nullable = false, length = 512)
    private String payloadType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(nullable = false, length = 64)
    private String traceId;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;
}
