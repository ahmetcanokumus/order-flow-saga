package com.saga.choreography.stock.repository;

import com.saga.choreography.stock.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryEntity i where i.sku = :sku")
    Optional<InventoryEntity> findBySkuForUpdate(@Param("sku") String sku);
}
