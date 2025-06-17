package ru.yandex.practicum.commerce.dto.store;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SetProductQuantityStateRequest {
    @NotNull
    UUID productId; // идентификатор товара

    //    @NotNull
    QuantityState quantityState; // статус, перечисляющий состояние остатка как свойства товара
}
