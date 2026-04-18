package com.saga.choreography.order.web;

import com.saga.choreography.order.service.OrderCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCommandService orderCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreateOrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderCommandService.createPendingOrder(request);
    }
}
