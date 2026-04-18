package com.saga.choreography.order.web;

import java.util.UUID;

public record CreateOrderResponse(UUID orderId, String traceId) {
}
