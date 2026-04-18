package com.saga.choreography.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
public class InventoryEntity {

    @Id
    @Column(length = 64)
    private String sku;

    @Column(nullable = false)
    private int availableQuantity;

    @Version
    private Long version;
}
