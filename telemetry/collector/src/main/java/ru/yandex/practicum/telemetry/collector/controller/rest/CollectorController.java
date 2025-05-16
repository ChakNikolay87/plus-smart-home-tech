package ru.yandex.practicum.telemetry.collector.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.telemetry.collector.handler.hub.rest.HubEventDispatcher;
import ru.yandex.practicum.telemetry.collector.handler.sensor.rest.SensorEventDispatcher;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class CollectorController {

    private final SensorEventDispatcher sensorDispatcher;
    private final HubEventDispatcher hubDispatcher;

    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void collectSensor(@Valid @RequestBody SensorEvent event) {
        sensorDispatcher.dispatch(event);
    }

    @PostMapping("/hubs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void collectHub(@Valid @RequestBody HubEvent event) {
        hubDispatcher.dispatch(event);
    }
}
