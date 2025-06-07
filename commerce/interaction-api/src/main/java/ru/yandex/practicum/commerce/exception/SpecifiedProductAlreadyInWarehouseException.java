package ru.yandex.practicum.commerce.exception;

public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {
    public SpecifiedProductAlreadyInWarehouseException(final String message) {
        super(message);
    }
}
