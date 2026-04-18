package com.saga.choreography.stock.service;

import com.saga.choreography.common.event.BaseEvent;
import com.saga.choreography.common.event.OrderCreatedEvent;
import com.saga.choreography.common.event.PaymentFailedEvent;
import com.saga.choreography.common.event.PaymentProcessedEvent;
import com.saga.choreography.common.event.StockLimitExceededEvent;
import com.saga.choreography.common.event.StockReservedEvent;
import com.saga.choreography.stock.entity.InventoryEntity;
import com.saga.choreography.stock.entity.ReservationState;
import com.saga.choreography.stock.entity.StockReservationEntity;
import com.saga.choreography.stock.repository.InventoryRepository;
import com.saga.choreography.stock.repository.StockReservationRepository;
import com.saga.choreography.stock.saga.StockDispatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockSagaService {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository stockReservationRepository;

    @Transactional
    public StockDispatch onOrderCreated(OrderCreatedEvent orderCreated) {
        var invOpt = inventoryRepository.findBySkuForUpdate(orderCreated.sku());
        if (invOpt.isEmpty()) {
            BaseEvent base = newBase(orderCreated);
            return new StockDispatch.LimitExceeded(
                    new StockLimitExceededEvent(base, orderCreated.sku(), orderCreated.quantity())
            );
        }
        InventoryEntity inv = invOpt.get();
        if (inv.getAvailableQuantity() < orderCreated.quantity()) {
            BaseEvent base = newBase(orderCreated);
            return new StockDispatch.LimitExceeded(
                    new StockLimitExceededEvent(base, orderCreated.sku(), orderCreated.quantity())
            );
        }
        inv.setAvailableQuantity(inv.getAvailableQuantity() - orderCreated.quantity());
        inventoryRepository.save(inv);

        StockReservationEntity reservation = new StockReservationEntity();
        reservation.setOrderId(orderCreated.orderId());
        reservation.setSku(orderCreated.sku());
        reservation.setQuantity(orderCreated.quantity());
        reservation.setState(ReservationState.RESERVED);
        stockReservationRepository.save(reservation);

        BaseEvent base = newBase(orderCreated);
        return new StockDispatch.Reserved(
                new StockReservedEvent(base, orderCreated.sku(), orderCreated.quantity(), orderCreated.totalAmount())
        );
    }

    @Transactional
    public void commitReservation(PaymentProcessedEvent paymentProcessed) {
        StockReservationEntity res = stockReservationRepository
                .findByOrderIdAndState(paymentProcessed.orderId(), ReservationState.RESERVED)
                .orElseThrow(() -> new IllegalStateException("No RESERVED stock for order " + paymentProcessed.orderId()));
        res.setState(ReservationState.COMMITTED);
        stockReservationRepository.save(res);
    }

    @Transactional
    public void releaseReservation(PaymentFailedEvent paymentFailed) {
        StockReservationEntity res = stockReservationRepository
                .findByOrderIdAndState(paymentFailed.orderId(), ReservationState.RESERVED)
                .orElseThrow(() -> new IllegalStateException("No RESERVED stock for order " + paymentFailed.orderId()));

        InventoryEntity inv = inventoryRepository
                .findBySkuForUpdate(res.getSku())
                .orElseThrow(() -> new IllegalStateException("Missing inventory for sku " + res.getSku()));
        inv.setAvailableQuantity(inv.getAvailableQuantity() + res.getQuantity());
        inventoryRepository.save(inv);

        res.setState(ReservationState.RELEASED);
        stockReservationRepository.save(res);
    }

    private static BaseEvent newBase(OrderCreatedEvent source) {
        return new BaseEvent(
                source.orderId(),
                UUID.randomUUID(),
                source.traceId(),
                Instant.now()
        );
    }
}
