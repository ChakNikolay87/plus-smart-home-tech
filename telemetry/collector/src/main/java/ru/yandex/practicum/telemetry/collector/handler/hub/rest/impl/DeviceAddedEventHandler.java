package ru.yandex.practicum.telemetry.collector.handler.hub.rest.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.handler.hub.rest.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.mapper.rest.HubEventMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEventType;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler<DeviceAddedEvent> {
    private final KafkaEventProducer producer;
    private static final String TOPIC = "telemetry.hubs.v1";

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }

    @Override
    public void handle(DeviceAddedEvent event) {
        var avro = HubEventMapper.toAvro(event);
        producer.send(TOPIC, event.getId(), avro);
    }
}
