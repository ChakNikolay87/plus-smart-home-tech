package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.order.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

/**
 * API для работы с заказами
 */
public interface OrderApi {
    /**
     * Получение заказов пользователя
     *
     * @param username - имя пользователя
     * @param pageable - объект пагинации (номер страницы, кол-во страниц и параметры сортировки)
     * @return - список заказов пользователя
     */
    @GetMapping
    List<OrderDto> getClientOrders(@RequestParam @NotBlank String username, Pageable pageable);

    /**
     * Создание нового заказа
     *
     * @param request - запрос на создание заказа
     * @return - созданный заказ
     */
    @PutMapping
    OrderDto createNewOrder(@RequestBody @Valid CreateNewOrderRequest request);

    /**
     * Возврат товаров из заказа
     *
     * @param request - запрос на возврат
     * @return - обновленный заказ
     */
    @PostMapping("/return")
    OrderDto productReturn(@RequestBody @Valid ProductReturnRequest request);

    /**
     * Оплата заказа
     *
     * @param orderId - идентификатор заказа
     * @return - заказ пользователя после оплаты
     */
    @PostMapping("/payment")
    OrderDto payment(@RequestBody @NotNull UUID orderId);

    /**
     * Оплата заказа произошла с ошибкой
     *
     * @param orderId - идентификатор заказа
     * @return - заказ пользователя после ошибки оплаты
     */
    @PostMapping("/payment/failed")
    OrderDto paymentFailed(@RequestBody @NotNull UUID orderId);

    /**
     * Доставка заказа
     *
     * @param orderId - идентификатор заказа
     * @return - заказ пользователя после доставки
     */
    @PostMapping("/delivery")
    OrderDto delivery(@RequestBody @NotNull UUID orderId);

    /**
     * Доставка заказа произошла с ошибкой
     *
     * @param orderId - идентификатор заказа
     * @return - заказ пользователя после ошибки доставки
     */
    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@RequestBody @NotNull UUID orderId);

    /**
     * Завершение заказа
     *
     * @param orderId - идентификатор заказа
     * @return - заказ пользователя после всех стадий и завершенный
     */
    @PostMapping("/completed")
    OrderDto completed(@RequestBody @NotNull UUID orderId);

    /**
     * Расчет стоимости заказа
     *
     * @param orderId - идентификатор заказа
     * @return - заказ пользователя с расчетом общей стоимости
     */
    @PostMapping("/calculate/total")
    OrderDto calculateTotalCost(@RequestBody @NotNull UUID orderId);

    /**
     * Расчет стоимости доставки заказа
     *
     * @param orderId - идентификатор заказа
     * @return - заказ пользователя с расчетом доставки
     */
    @PostMapping("/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody @NotNull UUID orderId);

    /**
     * Сборка заказа
     *
     * @param orderId - идентификатор заказа
     * @return - заказ пользователя после сборки
     */
    @PostMapping("/assembly")
    OrderDto assembly(@RequestBody @NotNull UUID orderId);

    /**
     * Сборка заказа произошла с ошибкой
     *
     * @param orderId - идентификатор заказа
     * @return - заказ пользователя после ошибки сборки
     */
    @PostMapping("/assembly/failed")
    OrderDto assemblyFailed(@RequestBody @NotNull UUID orderId);
}
