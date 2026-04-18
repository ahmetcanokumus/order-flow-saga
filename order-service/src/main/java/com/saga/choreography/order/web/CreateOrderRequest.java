package com.saga.choreography.order.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotNull BigDecimal totalAmount,
        @NotBlank String sku,
        @Min(1) int quantity
) {
}
