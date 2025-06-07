package ru.yandex.practicum.commerce.exception;

public class ShoppingCartDeactivationException extends RuntimeException {
    public ShoppingCartDeactivationException(String message) {
        super(message);
    }
}
