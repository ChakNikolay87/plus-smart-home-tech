package ru.yandex.practicum.telemetry.collector.handler.sensor.rest;

import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType;

public interface SensorEventHandler<T extends SensorEvent> {
    SensorEventType getType();

    void handle(T event);
}
