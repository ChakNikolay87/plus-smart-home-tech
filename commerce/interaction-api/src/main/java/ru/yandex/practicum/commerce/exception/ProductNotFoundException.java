package ru.yandex.practicum.commerce.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(final String message) {
        super(message);
    }
}
