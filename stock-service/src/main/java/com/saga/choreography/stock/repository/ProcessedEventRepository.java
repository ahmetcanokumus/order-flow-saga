package com.saga.choreography.stock.repository;

import com.saga.choreography.stock.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, Long> {

    boolean existsByOrderIdAndMessageEventId(UUID orderId, UUID messageEventId);
}
