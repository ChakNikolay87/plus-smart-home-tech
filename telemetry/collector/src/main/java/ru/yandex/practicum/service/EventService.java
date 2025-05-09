package ru.yandex.practicum.service;

public interface EventService<T> {

    void collect(T event);
}
