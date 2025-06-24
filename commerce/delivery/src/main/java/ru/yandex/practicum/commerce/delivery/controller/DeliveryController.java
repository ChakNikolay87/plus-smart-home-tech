package ru.yandex.practicum.commerce.delivery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.DeliveryApi;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DeliveryController implements DeliveryApi {
    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        log.info("Received request for create new delivery: {}", deliveryDto);
        return deliveryService.planDelivery(deliveryDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @Override
    public void deliverySuccessful(UUID orderId) {
        log.info("Received request to change status on successful for order ID: {}", orderId);
        deliveryService.deliverySuccessful(orderId);
    }

    @ResponseStatus(HttpStatus.OK)
    @Override
    public void deliveryPicked(UUID orderId) {
        log.info("Received request to change status on picked for order ID: {}", orderId);
        deliveryService.deliveryPicked(orderId);
    }

    @ResponseStatus(HttpStatus.OK)
    @Override
    public void deliveryFailed(UUID orderId) {
        log.info("Received request to change status on failed for order ID: {}", orderId);
        deliveryService.deliveryFailed(orderId);
    }

    @Override
    public BigDecimal deliveryCost(OrderDto orderDto) {
        log.info("Received request to calculate delivery cost for order ID: {}", orderDto);
        return deliveryService.deliveryCost(orderDto);
    }
}
