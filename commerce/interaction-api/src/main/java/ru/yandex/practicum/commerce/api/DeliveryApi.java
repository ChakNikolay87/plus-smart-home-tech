package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * API для расчёта, проведения доставки
 */
public interface DeliveryApi {
    /**
     * Создание новой доставки в БД
     *
     * @param deliveryDto - доставка для сохранения
     * @return - доставка с присвоенным идентификатором
     */
    @PutMapping
    DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto deliveryDto);

    /**
     * Эмуляция успешной доставки товара
     *
     * @param orderId - идентификатор заказа
     */
    @PostMapping("/successful")
    void deliverySuccessful(@RequestBody @NotNull UUID orderId);

    /**
     * Эмуляция получения товара в доставку
     *
     * @param orderId - идентификатора заказа
     */
    @PostMapping("/picked")
    void deliveryPicked(@RequestBody @NotNull UUID orderId);

    /**
     * Эмуляция неудачного вручения товара
     *
     * @param orderId - идентификатор заказа
     */
    @PostMapping("/failed")
    void deliveryFailed(@RequestBody @NotNull UUID orderId);

    /**
     * Расчёт полной стоимости доставки заказа
     *
     * @param orderDto - заказ для расчета
     * @return - полная стоимость заказа
     */
    @PostMapping("/cost")
    BigDecimal deliveryCost(@RequestBody @NotNull OrderDto orderDto);
}
