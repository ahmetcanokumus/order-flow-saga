package com.saga.choreography.stock.repository;

import com.saga.choreography.stock.entity.OutboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessageEntity, Long> {

    List<OutboxMessageEntity> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
