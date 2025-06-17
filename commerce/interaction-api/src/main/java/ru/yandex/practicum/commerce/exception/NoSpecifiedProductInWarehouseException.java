package ru.yandex.practicum.commerce.exception;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {
    public NoSpecifiedProductInWarehouseException(final String message) {
        super(message);
    }
}
