package ru.yandex.practicum.commerce.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.PaymentApi;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Override
    public PaymentDto payment(OrderDto order) {
        log.info("Received request to payment for order {}", order);
        return paymentService.payment(order);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto order) {
        log.info("Received request to calculate total cost for order {}", order);
        return paymentService.getTotalCost(order);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        log.info("Received request to payment success for id {}", paymentId);
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public BigDecimal productCost(OrderDto order) {
        log.info("Received request to calculate products cost for order {}", order);
        return paymentService.productCost(order);
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        log.info("Received request to payment failed for id {}", paymentId);
        paymentService.paymentFailed(paymentId);
    }
}
