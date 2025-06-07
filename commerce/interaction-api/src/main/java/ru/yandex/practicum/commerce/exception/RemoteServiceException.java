package ru.yandex.practicum.commerce.exception;

public class RemoteServiceException extends RuntimeException {
    public RemoteServiceException(final String message) {
        super(message);
    }
}
