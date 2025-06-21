package ru.yandex.practicum.commerce.exception;

public class NoPaymentFoundException extends RuntimeException {
    public NoPaymentFoundException(final String message) {
        super(message);
    }
}
