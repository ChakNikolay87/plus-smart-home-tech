package ru.yandex.practicum.commerce.payment.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.dto.payment.PaymentState;
import ru.yandex.practicum.commerce.dto.store.ProductDto;
import ru.yandex.practicum.commerce.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.exception.NoPaymentFoundException;
import ru.yandex.practicum.commerce.exception.RemoteServiceException;
import ru.yandex.practicum.commerce.payment.feign.DeliveryClient;
import ru.yandex.practicum.commerce.payment.feign.OrderClient;
import ru.yandex.practicum.commerce.payment.feign.ShoppingStoreClient;
import ru.yandex.practicum.commerce.payment.model.Payment;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {
    private static final BigDecimal TAX_RATE = new BigDecimal(10); // налог НДС в процентах
    private final ModelMapper modelMapper;
    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final DeliveryClient deliveryClient;
    private final OrderClient orderClient;

    @Transactional
    public PaymentDto payment(OrderDto order) {
        log.info("Saving payment for order {}", order);
        BigDecimal totalPayment = getTotalCost(order);
        BigDecimal deliveryTotal;

        try {
            deliveryTotal = deliveryClient.deliveryCost(order);
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new NoOrderFoundException("Order with ID = " + order.getOrderId() + " not found");
            }
            throw new RemoteServiceException("Error in the remote service 'delivery'");
        }

        BigDecimal feeTotal = productCost(order).multiply(TAX_RATE.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .totalPayment(totalPayment)
                .deliveryTotal(deliveryTotal)
                .feeTotal(feeTotal)
                .paymentState(PaymentState.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved: " + savedPayment);
        return modelMapper.map(savedPayment, PaymentDto.class);
    }

    public BigDecimal getTotalCost(OrderDto order) {
        log.info("Calculating total cost for order {}", order);
        BigDecimal productCost = productCost(order);
        BigDecimal tax = productCost.multiply(TAX_RATE.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));
        BigDecimal deliveryCost;
        try {
            deliveryCost = deliveryClient.deliveryCost(order);
        } catch (FeignException ex) {
            throw new RemoteServiceException("Error in the remote service 'order'");
        }
        log.info("Total cost for order {} has been calculated", order);
        return productCost.add(tax).add(deliveryCost);
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        log.info("Setting successful state for payment with ID: {}", paymentId);
        Payment payment = getPaymentById(paymentId);
        payment.setPaymentState(PaymentState.SUCCESS);
        try {
            orderClient.payment(payment.getOrderId());
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new NoOrderFoundException("No order found with id " + payment.getOrderId());
            }
            throw new RemoteServiceException("Error in the remote service 'order'");
        }
        paymentRepository.save(payment);
        log.info("Payment successful state has been set for payment with ID: {}", paymentId);
    }

    public BigDecimal productCost(OrderDto order) {
        log.info("Calculating product cost for order {}", order);
        BigDecimal productCost = BigDecimal.ZERO;
        for (Map.Entry<UUID, Long> entry : order.getProducts().entrySet()) {
            ProductDto productDto = shoppingStoreClient.getProduct(entry.getKey());
            productCost = productCost.add(productDto.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        log.info("Calculated product cost for order {}", order);
        return productCost;
    }

    public void paymentFailed(UUID paymentId) {
        log.info("Setting failed state for payment with ID: {}", paymentId);
        Payment payment = getPaymentById(paymentId);
        payment.setPaymentState(PaymentState.FAILED);
        try {
            orderClient.payment(payment.getOrderId());
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new NoOrderFoundException("No order found with id " + payment.getOrderId());
            }
            throw new RemoteServiceException("Error in the remote service 'order'");
        }
        paymentRepository.save(payment);
        log.info("Payment failed state has been set for payment with ID: {}", paymentId);
    }

    private Payment getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoPaymentFoundException("No payment found for ID = " + paymentId));
    }
}
