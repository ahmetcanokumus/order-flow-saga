package com.saga.choreography.stock.repository;

import com.saga.choreography.stock.entity.ReservationState;
import com.saga.choreography.stock.entity.StockReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservationEntity, UUID> {

    Optional<StockReservationEntity> findByOrderIdAndState(UUID orderId, ReservationState state);
}
