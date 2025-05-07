package ru.yandex.practicum.service.sensor;

import ru.yandex.practicum.model.sensor.SensorEvent;

public interface SensorEventService {

    void collect(SensorEvent sensorEvent);
}
