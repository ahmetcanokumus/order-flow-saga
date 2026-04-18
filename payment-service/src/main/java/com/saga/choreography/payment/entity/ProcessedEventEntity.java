package com.saga.choreography.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "processed_events",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_message_event", columnNames = {"order_id", "message_event_id"})
)
@Getter
@Setter
public class ProcessedEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, columnDefinition = "uuid")
    private UUID orderId;

    @Column(name = "message_event_id", nullable = false, columnDefinition = "uuid")
    private UUID messageEventId;

    @Column(nullable = false, length = 128)
    private String eventType;

    @Column(nullable = false)
    private Instant processedAt;
}
