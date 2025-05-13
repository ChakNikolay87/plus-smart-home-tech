package ru.yandex.practicum.service.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.hub.*;
import ru.yandex.practicum.service.EventService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventServiceImpl implements EventService<HubEvent> {
    private final KafkaClient kafkaClient;

    @Value(value = "${hubEventTopic}")
    private String topic;

    @Override
    public void collect(HubEvent event) {
        HubEventAvro hubEventAvro = mapToAvro(event);
        kafkaClient.getProducer().send(new ProducerRecord<>(topic, hubEventAvro));
        log.info("To topic {} sent message with hub event {}", topic, event);
    }

    private HubEventAvro mapToAvro(HubEvent event) {
        Object payload = switch (event) {
            case DeviceAddedEvent deviceAddedEvent -> mapToDeviceAddedAvro(deviceAddedEvent);
            case DeviceRemovedEvent deviceRemovedEvent -> mapToDeviceRemovedAvro(deviceRemovedEvent);
            case ScenarioAddedEvent scenarioAddedEvent -> mapToScenarioAddedAvro(scenarioAddedEvent);
            case ScenarioRemovedEvent scenarioRemovedEvent -> mapToScenarioRemovedAvro(scenarioRemovedEvent);
            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        };

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private DeviceAddedEventAvro mapToDeviceAddedAvro(DeviceAddedEvent event) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(DeviceTypeAvro.valueOf(event.getDeviceType().name()))
                .build();
    }

    private DeviceRemovedEventAvro mapToDeviceRemovedAvro(DeviceRemovedEvent event) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
    }

    private ScenarioAddedEventAvro mapToScenarioAddedAvro(ScenarioAddedEvent event) {
        List<DeviceActionAvro> deviceActionAvroList = event.getActions().stream()
                .map(this::mapDeviceAction)
                .toList();
        List<ScenarioConditionAvro> scenarioConditionAvroList = event.getConditions().stream()
                .map(this::mapScenarioCondition)
                .toList();
        return ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setActions(deviceActionAvroList)
                .setConditions(scenarioConditionAvroList)
                .build();
    }

    private ScenarioRemovedEventAvro mapToScenarioRemovedAvro(ScenarioRemovedEvent event) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
    }

    private DeviceActionAvro mapDeviceAction(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setSensorId(action.getSensorId())
                .setValue(action.getValue())
                .build();
    }

    private ScenarioConditionAvro mapScenarioCondition(ScenarioCondition condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setValue(condition.getValue())
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .build();
    }
}
