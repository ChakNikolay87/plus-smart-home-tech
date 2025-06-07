package ru.yandex.practicum.commerce.exception;

public class NotAuthorizedUserException extends RuntimeException {
    public NotAuthorizedUserException(final String message) {
        super(message);
    }
}
