package ru.yandex.practicum.exception;

public class HandlerNotFound extends RuntimeException{
    public HandlerNotFound(String message) {
        super(message);
    }
}
