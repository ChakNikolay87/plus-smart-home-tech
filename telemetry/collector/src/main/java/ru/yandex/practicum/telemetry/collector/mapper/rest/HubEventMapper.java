package ru.yandex.practicum.telemetry.collector.mapper.rest;

import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.model.hub.*;

import java.util.List;
import java.util.stream.Collectors;

public class HubEventMapper {

    public static HubEventAvro toAvro(HubEvent event) {
        long timestamp = event.getTimestamp().toEpochMilli();
        String hubId = event.getHubId();

        if (event instanceof DeviceAddedEvent added) {
            DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                    .setId(added.getId())
                    .setType(DeviceTypeAvro.valueOf(added.getDeviceType().name()))
                    .build();
            return buildAvro(hubId, timestamp, payload);
        }

        if (event instanceof DeviceRemovedEvent removed) {
            DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                    .setId(removed.getId())
                    .build();
            return buildAvro(hubId, timestamp, payload);
        }

        if (event instanceof ScenarioAddedEvent scenario) {
            List<ScenarioConditionAvro> conditions = scenario.getConditions().stream()
                    .map(HubEventMapper::mapCondition)
                    .collect(Collectors.toList());
            List<DeviceActionAvro> actions = scenario.getActions().stream()
                    .map(HubEventMapper::mapAction)
                    .collect(Collectors.toList());
            ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                    .setName(scenario.getName())
                    .setConditions(conditions)
                    .setActions(actions)
                    .build();
            return buildAvro(hubId, timestamp, payload);
        }

        if (event instanceof ScenarioRemovedEvent removed) {
            ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                    .setName(removed.getName())
                    .build();
            return buildAvro(hubId, timestamp, payload);
        }

        throw new IllegalArgumentException(String.format("Неподдерживаемый тип события хаба: %s",
                event.getClass().getSimpleName()));
    }

    private static HubEventAvro buildAvro(String hubId, long timestamp, Object payload) {
        return HubEventAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }

    private static ScenarioConditionAvro mapCondition(ScenarioCondition condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()));

        if (condition.getValue() != null) {
            builder.setValue(condition.getValue());
        }

        return builder.build();
    }

    private static DeviceActionAvro mapAction(DeviceAction action) {
        DeviceActionAvro.Builder builder = DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()));

        if (action.getValue() != null) {
            builder.setValue(action.getValue());
        }

        return builder.build();
    }
}
