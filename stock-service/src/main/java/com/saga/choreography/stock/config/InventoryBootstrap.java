package com.saga.choreography.stock.config;

import com.saga.choreography.stock.entity.InventoryEntity;
import com.saga.choreography.stock.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryBootstrap implements ApplicationRunner {

    public static final String DEMO_SKU = "DEMO-SKU";

    private final InventoryRepository inventoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (inventoryRepository.existsById(DEMO_SKU)) {
            return;
        }
        InventoryEntity row = new InventoryEntity();
        row.setSku(DEMO_SKU);
        row.setAvailableQuantity(100);
        inventoryRepository.save(row);
        log.info("Seeded inventory sku={} qty={}", DEMO_SKU, 100);
    }
}
