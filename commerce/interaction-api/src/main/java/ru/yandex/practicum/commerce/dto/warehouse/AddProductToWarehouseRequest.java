package ru.yandex.practicum.commerce.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Запрос на увеличение единиц товара по его идентификатору
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddProductToWarehouseRequest {
    UUID productId; // идентификатор товара в БД

    @NotNull
    @Min(value = 1, message = "Quantity must be equal to 1 or more")
    Long quantity; // количество единиц товара для добавления на склад
}
