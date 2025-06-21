package ru.yandex.practicum.commerce.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.OrderApi;
import ru.yandex.practicum.commerce.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OrderController implements OrderApi {
    private final OrderService orderService;

    @Override
    public List<OrderDto> getClientOrders(String username, Pageable pageable) {
        log.info("Received request to get client orders for user: {}", username);
        return orderService.getClientOrders(username, pageable);
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Received request to create new order: {}", request);
        return orderService.createNewOrder(request);
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("Received request to product return order: {}", request);
        return orderService.productReturn(request);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        log.info("Received request to payment order: {}", orderId);
        return orderService.payment(orderId);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Received request to failed payment order: {}", orderId);
        return orderService.paymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        log.info("Received request to delivery order: {}", orderId);
        return orderService.delivery(orderId);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Received request to delivery failed payment order: {}", orderId);
        return orderService.deliveryFailed(orderId);
    }

    @Override
    public OrderDto completed(UUID orderId) {
        log.info("Received request to completed order: {}", orderId);
        return orderService.completed(orderId);
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Received request to calculate total cost for order: {}", orderId);
        return orderService.calculateTotalCost(orderId);
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Received request to calculate delivery cost for order: {}", orderId);
        return orderService.calculateDeliveryCost(orderId);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        log.info("Received request to assembly order: {}", orderId);
        return orderService.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Received request to assembly failed payment order: {}", orderId);
        return orderService.assemblyFailed(orderId);
    }
}
