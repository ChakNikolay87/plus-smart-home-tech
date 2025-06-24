package ru.yandex.practicum.commerce.dto.delivery;

/**
 * Статусы доставки
 */
public enum DeliveryState {
    CREATED,
    IN_PROGRESS,
    DELIVERED,
    FAILED,
    CANCELLED
}
