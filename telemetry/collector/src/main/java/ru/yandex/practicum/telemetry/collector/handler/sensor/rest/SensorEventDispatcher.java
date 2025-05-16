package ru.yandex.practicum.telemetry.collector.handler.sensor.rest;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SensorEventDispatcher {

    private final List<SensorEventHandler<?>> handlers;
    private final Map<SensorEventType, SensorEventHandler> handlerMap = new HashMap<>();

    @PostConstruct
    void init() {
        for (SensorEventHandler<?> handler : handlers) {
            handlerMap.put(handler.getType(), handler);
        }
    }

    public void dispatch(SensorEvent event) {
        SensorEventHandler handler = handlerMap.get(event.getType());
        if (handler == null) {
            throw new IllegalArgumentException(String.format("Нет обработчика для типа: %s",
                    event.getType()));
        }
        handler.handle(event);
    }
}

