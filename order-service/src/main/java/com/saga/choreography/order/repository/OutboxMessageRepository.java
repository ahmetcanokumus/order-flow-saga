package com.saga.choreography.order.repository;

import com.saga.choreography.order.entity.OutboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessageEntity, Long> {

    List<OutboxMessageEntity> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
