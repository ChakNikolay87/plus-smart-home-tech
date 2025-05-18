package ru.yandex.practicum.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.device.HubEvent;
import ru.yandex.practicum.model.device.events.DeviceAddedEvent;
import ru.yandex.practicum.model.device.events.DeviceRemovedEvent;
import ru.yandex.practicum.model.device.events.ScenarioAddedEvent;
import ru.yandex.practicum.model.device.events.ScenarioRemovedEvent;
import ru.yandex.practicum.model.device.types.DeviceType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class HubEventMapper {

    private static final Map<Class<?>, Function<HubEventAvro, HubEvent>> mappers = new HashMap<>();

    public HubEventMapper() {
        // Инициализация маппера с обработчиками для разных типов payload
        mappers.put(DeviceAddedEventAvro.class, this::mapDeviceAddedEvent);
        mappers.put(DeviceRemovedEventAvro.class, this::mapDeviceRemovedEvent);
        mappers.put(ScenarioAddedEventAvro.class, this::mapScenarioAddedEvent);
        mappers.put(ScenarioRemovedEventAvro.class, this::mapScenarioRemovedEvent);
    }

    // Основной метод для маппинга HubEventAvro
    public static HubEvent mapToDomain(HubEventAvro avro) {
        Object payload = avro.getPayload();
        if (mappers.containsKey(payload.getClass())) {
            return mappers.get(payload.getClass()).apply(avro);
        }

        log.error("Unknown event type received: {}", payload.getClass());
        throw new IllegalArgumentException("Unknown event type: " + payload.getClass());
    }

    private DeviceAddedEvent mapDeviceAddedEvent(HubEventAvro avro) {
        DeviceAddedEventAvro payload = (DeviceAddedEventAvro) avro.getPayload();

        return DeviceAddedEvent.builder()
                .hubId(avro.getHubId())
                .timestamp(avro.getTimestamp())
                .id(payload.getId())
                .deviceType(DeviceType.valueOf(payload.getType().name()))
                .build();
    }

    private DeviceRemovedEvent mapDeviceRemovedEvent(HubEventAvro avro) {
        DeviceRemovedEventAvro payload = (DeviceRemovedEventAvro) avro.getPayload();
        return DeviceRemovedEvent.builder()
                .hubId(avro.getHubId())
                .timestamp(avro.getTimestamp())
                .id(payload.getId())
                .build();
    }

    private ScenarioAddedEvent mapScenarioAddedEvent(HubEventAvro avro) {
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) avro.getPayload();
        return ScenarioAddedEvent.builder()
                .hubId(avro.getHubId())
                .timestamp(avro.getTimestamp())
                .name(payload.getName())
                .conditions(ConditionsMapper.condAvroListToJava(payload.getConditions()))
                .actions(ActionMapper.devActAvroListToJavaList(payload.getActions()))
                .build();
    }

    private ScenarioRemovedEvent mapScenarioRemovedEvent(HubEventAvro avro) {
        ScenarioRemovedEventAvro payload = (ScenarioRemovedEventAvro) avro.getPayload();
        return ScenarioRemovedEvent.builder()
                .hubId(avro.getHubId())
                .timestamp(avro.getTimestamp())
                .name(payload.getName())
                .build();
    }
}
