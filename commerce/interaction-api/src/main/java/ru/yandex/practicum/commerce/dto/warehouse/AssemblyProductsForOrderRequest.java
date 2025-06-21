package ru.yandex.practicum.commerce.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

/**
 * Запрос на сбор заказа из товаров
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AssemblyProductsForOrderRequest {
    @NotNull
    Map<UUID, Long> products; // ID товара и его количество

    @NotNull
    UUID orderId; // идентификатор заказа в БД
}
