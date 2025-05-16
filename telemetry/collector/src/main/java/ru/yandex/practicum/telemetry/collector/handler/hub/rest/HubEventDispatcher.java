package ru.yandex.practicum.telemetry.collector.handler.hub.rest;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HubEventDispatcher {

    private final List<HubEventHandler<?>> handlers;
    private final Map<HubEventType, HubEventHandler> handlerMap = new HashMap<>();

    @PostConstruct
    void init() {
        for (HubEventHandler<?> handler : handlers) {
            handlerMap.put(handler.getType(), handler);
        }
    }

    public void dispatch(HubEvent event) {
        HubEventHandler handler = handlerMap.get(event.getType());
        if (handler == null) {
            throw new IllegalArgumentException(String.format("Нет обработчика для типа: %s",
                    event.getType()));
        }
        handler.handle(event);
    }
}
