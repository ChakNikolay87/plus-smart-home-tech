package ru.yandex.practicum.telemetry.collector.handler.hub.rest.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.handler.hub.rest.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.mapper.rest.HubEventMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler<ScenarioAddedEvent> {
    private final KafkaEventProducer producer;
    private static final String TOPIC = "telemetry.hubs.v1";

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    public void handle(ScenarioAddedEvent event) {
        var avro = HubEventMapper.toAvro(event);
        producer.send(TOPIC, event.getName(), avro);
    }
}
