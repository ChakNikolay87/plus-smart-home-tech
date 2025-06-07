package ru.yandex.practicum.commerce.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Запрос на добавление нового товара на склад
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewProductInWarehouseRequest {
    @NotNull
    UUID productId; // идентификатор товара в БД

    boolean fragile; // признак хрупкости

    @NotNull
    DimensionDto dimension; // размеры товара

    @NotNull
    @Min(value = 1, message = "Weight must be 1 or greater")
    Double weight; // вес товара
}
