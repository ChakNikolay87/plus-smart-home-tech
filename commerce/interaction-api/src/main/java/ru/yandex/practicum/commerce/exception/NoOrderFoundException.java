package ru.yandex.practicum.commerce.exception;

public class NoOrderFoundException extends RuntimeException {
    public NoOrderFoundException(final String message) {
        super(message);
    }
}
