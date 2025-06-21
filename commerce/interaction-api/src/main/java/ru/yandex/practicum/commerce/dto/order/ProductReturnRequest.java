package ru.yandex.practicum.commerce.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

/**
 * Запрос на возврат товара
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductReturnRequest {
    UUID orderId; // идентификатор заказа

    @NotNull
    Map<UUID, Long> products; // отображение идентификатора товара на отобранное количество

}
