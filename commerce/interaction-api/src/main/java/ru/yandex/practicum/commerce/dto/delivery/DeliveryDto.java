package ru.yandex.practicum.commerce.dto.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;

import java.util.UUID;

/**
 * Информация о доставке
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DeliveryDto {
    @NotNull
    UUID deliveryId; // идентификатор доставки

    @NotNull
    AddressDto fromAddress; // адрес отгрузки

    @NotNull
    AddressDto toAddress; // адрес доставки

    @NotNull
    UUID orderId; // идентификатор заказа

    @NotNull
    DeliveryState deliveryState; // статус доставки
}
