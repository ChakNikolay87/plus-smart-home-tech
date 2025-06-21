package ru.yandex.practicum.commerce.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;

/**
 * Новый заказ
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateNewOrderRequest {
    @NotBlank
    ShoppingCartDto shoppingCart; // корзина товаров в онлайн магазине

    @NotBlank
    AddressDto deliveryAddress; // адрес доставки
}
