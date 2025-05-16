package ru.yandex.practicum.telemetry.collector.handler.hub.rest;

import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEventType;

public interface HubEventHandler<T extends HubEvent> {
    HubEventType getType();

    void handle(T event);
}
