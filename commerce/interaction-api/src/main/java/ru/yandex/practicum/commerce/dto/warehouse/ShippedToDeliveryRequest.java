package ru.yandex.practicum.commerce.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Запрос на передачу в доставку товаров
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ShippedToDeliveryRequest {
    @NotNull
    UUID orderId; // идентификатор заказа в БД

    @NotNull
    UUID deliveryId; // идентификатор доставки в БД
}
