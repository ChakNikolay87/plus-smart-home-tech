package ru.yandex.practicum.telemetry.collector.handler.hub.rest.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.handler.hub.rest.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.mapper.rest.HubEventMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEventType;

@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler<DeviceRemovedEvent> {
    private final KafkaEventProducer producer;
    private static final String TOPIC = "telemetry.hubs.v1";

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @Override
    public void handle(DeviceRemovedEvent event) {
        var avro = HubEventMapper.toAvro(event);
        producer.send(TOPIC, event.getId(), avro);
    }
}
