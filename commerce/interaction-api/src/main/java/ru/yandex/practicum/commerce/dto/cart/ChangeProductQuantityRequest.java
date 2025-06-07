package ru.yandex.practicum.commerce.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Запрос на изменение количества единиц товара
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangeProductQuantityRequest {
    @NotNull
    UUID productId; // идентификатор товара

    @NotNull
    @PositiveOrZero(message = "The new quantity cannot be negative")
    Long newQuantity; // новое количество товара
}
