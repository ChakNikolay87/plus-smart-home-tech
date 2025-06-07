package ru.yandex.practicum.commerce.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

/**
 * Корзина товаров в онлайн магазине
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShoppingCartDto {
    @NotNull
    UUID shoppingCartId; // идентификатор корзины в БД

    @NotNull
    Map<UUID, Long> products; // отображение идентификатора товара на отобранное количество
}
