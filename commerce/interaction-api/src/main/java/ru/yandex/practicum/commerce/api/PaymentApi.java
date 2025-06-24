package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * API для расчёта, проведения оплат
 */
public interface PaymentApi {

    /**
     * Формирование оплаты для заказа (переход в платежный шлюз)
     *
     * @param order - заказ для формирования оплаты
     * @return - Сформированная оплата заказа (переход в платежный шлюз)
     */
    @PostMapping
    PaymentDto payment(@RequestBody @Valid OrderDto order);

    /**
     * Расчёт полной стоимости заказа
     *
     * @param order - заказ для расчета
     * @return - полная стоимость заказа
     */
    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@RequestBody @Valid OrderDto order);

    /**
     * Метод для эмуляции успешной оплаты в платежном шлюзе
     *
     * @param paymentId - идентификатор платежа
     */
    @PostMapping("/refund")
    void paymentSuccess(@RequestBody @NotNull UUID paymentId);

    /**
     * Расчёт стоимости товаров в заказе
     *
     * @param order - заказ для расчета
     * @return - стоимость товаров в заказе
     */
    @PostMapping("/productCost")
    BigDecimal productCost(@RequestBody @NotNull OrderDto order);

    /**
     * Метод для эмуляции отказа в оплате платежного шлюза
     *
     * @param paymentId - идентификатор платежа
     */
    @PostMapping("/failed")
    void paymentFailed(@RequestBody @NotNull UUID paymentId);
}
