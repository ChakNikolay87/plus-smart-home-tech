package ru.yandex.practicum.commerce.exception;

public class NotEnoughInfoInOrderToCalculateException extends RuntimeException {
    NotEnoughInfoInOrderToCalculateException(final String message) {
        super(message);
    }
}
