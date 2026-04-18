package com.saga.choreography.payment.repository;

import com.saga.choreography.payment.entity.OutboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessageEntity, Long> {

    List<OutboxMessageEntity> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
