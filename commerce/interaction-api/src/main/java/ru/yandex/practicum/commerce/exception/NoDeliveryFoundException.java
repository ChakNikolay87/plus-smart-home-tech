package ru.yandex.practicum.commerce.exception;

public class NoDeliveryFoundException extends RuntimeException {
    public NoDeliveryFoundException(final String message) {
        super(message);
    }
}
