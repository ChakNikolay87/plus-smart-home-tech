package ru.yandex.practicum.commerce.dto.payment;

/**
 * Статусы оплаты
 */
public enum PaymentState {
    PENDING, // ожидает оплаты
    SUCCESS, // успешно оплачен
    FAILED // ошибка в процессе оплаты
}
