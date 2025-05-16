package ru.yandex.practicum.telemetry.collector.mapper.grpc;

import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;
import java.util.stream.Collectors;

public class GrpcHubEventMapper {

    public static HubEventAvro toAvro(HubEventProto event) {
        String hubId = event.getHubId();
        long timestamp = event.getTimestamp().getSeconds() * 1000L
                + event.getTimestamp().getNanos() / 1_000_000;

        return switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> {
                var data = event.getDeviceAdded();
                DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                        .setId(data.getId())
                        .setType(DeviceTypeAvro.valueOf(data.getType().name()))
                        .build();
                yield build(hubId, timestamp, payload);
            }

            case DEVICE_REMOVED -> {
                var data = event.getDeviceRemoved();
                DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                        .setId(data.getId())
                        .build();
                yield build(hubId, timestamp, payload);
            }

            case SCENARIO_ADDED -> {
                var data = event.getScenarioAdded();
                List<ScenarioConditionAvro> conditions = data.getConditionList().stream()
                        .map(GrpcHubEventMapper::mapCondition)
                        .collect(Collectors.toList());
                List<DeviceActionAvro> actions = data.getActionList().stream()
                        .map(GrpcHubEventMapper::mapAction)
                        .collect(Collectors.toList());
                ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                        .setName(data.getName())
                        .setConditions(conditions)
                        .setActions(actions)
                        .build();
                yield build(hubId, timestamp, payload);
            }

            case SCENARIO_REMOVED -> {
                var data = event.getScenarioRemoved();
                ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                        .setName(data.getName())
                        .build();
                yield build(hubId, timestamp, payload);
            }

            default -> throw new IllegalArgumentException(String.format("Неподдерживаемый тип события хаба: %s",
                    event.getPayloadCase()));
        };
    }

    private static HubEventAvro build(String hubId, long timestamp, Object payload) {
        return HubEventAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }

    private static ScenarioConditionAvro mapCondition(ScenarioConditionProto condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()));

        if (condition.hasBoolValue()) {
            builder.setValue(condition.getBoolValue());
        } else if (condition.hasIntValue()) {
            builder.setValue(condition.getIntValue());
        }

        return builder.build();
    }

    private static DeviceActionAvro mapAction(DeviceActionProto action) {
        DeviceActionAvro.Builder builder = DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()));

        if (action.hasValue()) {
            builder.setValue(action.getValue());
        }

        return builder.build();
    }
}
