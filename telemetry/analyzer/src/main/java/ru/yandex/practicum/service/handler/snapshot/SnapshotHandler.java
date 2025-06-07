package ru.yandex.practicum.service.handler.snapshot;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.OperationType;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.service.HubRouterGrpcClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotHandler {
    private final ScenarioRepository scenarioRepository;
    private final HubRouterGrpcClient hubRouterGrpcClient;

    @Transactional(readOnly = true)
    public void handle(SensorsSnapshotAvro snapshot) {
        List<Scenario> scenariosToExecute = scenarioRepository.findByHubId(snapshot.getHubId()).stream()
                .filter(o -> checkScenarioConditions(o.getConditions(), snapshot))
                .toList();

        if (scenariosToExecute.isEmpty()) return; // если нет подходящих сценариев, то выход

        scenariosToExecute.forEach(this::executeScenarioActions);
    }

    private boolean checkScenarioConditions(Map<String, Condition> conditions, SensorsSnapshotAvro snapshot) {
        Map<String, SensorStateAvro> sensorStateAvroMap = snapshot.getSensorsState();

        // если в сценарии нет условий, то false и выход
        if (conditions.isEmpty()) return false;

        // обработаем все условия сценария
        for (Map.Entry<String, Condition> entry : conditions.entrySet()) {
            String sensorId = entry.getKey(); // ID сенсора - это ключ к мапе условий
            // если в снимке состояния датчиков нет ID сенсора из условия, то false и выход
            if (!sensorStateAvroMap.containsKey(sensorId)) return false;
            // получим состояние датчика из снапшота по его ID
            SensorStateAvro sensorStateAvro = sensorStateAvroMap.get(sensorId);
            // сохраним отдельной условия из сценария для сенсора
            Condition condition = entry.getValue();
            // проверяем поочередно условия сценария, если хотя бы одно условие не выполняется, то false и выход
            if (!checkScenarioCondition(condition, sensorStateAvro)) return false;
        }
        return true;
    }

    private boolean checkScenarioCondition(Condition condition, SensorStateAvro sensorStateAvro) {
        // если нет состояния для сенсора, то false и выход
        if (sensorStateAvro == null) return false;

        // проверка выполнения условий по их типам
        switch (condition.getType()) {
            case MOTION -> {
                MotionSensorAvro motionSensorAvro = (MotionSensorAvro) sensorStateAvro.getData();
                int sensorValue = motionSensorAvro.getMotion() ? 1 : 0;
                return checkCondition(sensorValue, condition.getOperation(), condition.getValue());
            }
            case SWITCH -> {
                SwitchSensorAvro switchSensorAvro = (SwitchSensorAvro) sensorStateAvro.getData();
                int sensorValue = switchSensorAvro.getState() ? 1 : 0;
                return checkCondition(sensorValue, condition.getOperation(), condition.getValue());
            }
            case CO2LEVEL -> {
                ClimateSensorAvro climateSensorAvro = (ClimateSensorAvro) sensorStateAvro.getData();
                return checkCondition(climateSensorAvro.getCo2Level(), condition.getOperation(), condition.getValue());
            }
            case HUMIDITY -> {
                ClimateSensorAvro climateSensorAvro = (ClimateSensorAvro) sensorStateAvro.getData();
                return checkCondition(climateSensorAvro.getHumidity(), condition.getOperation(), condition.getValue());
            }
            case LUMINOSITY -> {
                LightSensorAvro lightSensorAvro = (LightSensorAvro) sensorStateAvro.getData();
                return checkCondition(lightSensorAvro.getLuminosity(), condition.getOperation(), condition.getValue());
            }
            case TEMPERATURE -> {
                if (sensorStateAvro.getData() instanceof ClimateSensorAvro temperatureSensorAvro) {
                    return checkCondition(temperatureSensorAvro.getTemperatureC(), condition.getOperation(), condition.getValue());
                } else {
                    TemperatureSensorAvro temperatureSensorAvro = (TemperatureSensorAvro) sensorStateAvro.getData();
                    return checkCondition(temperatureSensorAvro.getTemperatureC(), condition.getOperation(), condition.getValue());
                }
            }
            default -> throw new IllegalStateException("Unexpected condition type: " + condition.getType());
        }
    }

    private boolean checkCondition(int sensorValue, OperationType operation, int conditionValue) {
        return switch (operation) {
            case EQUALS -> sensorValue == conditionValue;
            case GREATER_THAN -> sensorValue > conditionValue;
            case LOWER_THAN -> sensorValue < conditionValue;
        };
    }

    private void executeScenarioActions(Scenario scenario) {
        for (Map.Entry<String, Action> actionEntry : scenario.getActions().entrySet()) {
            String actionId = actionEntry.getKey();
            Action action = actionEntry.getValue();

            DeviceActionProto deviceActionProto = DeviceActionProto.newBuilder()
                    .setSensorId(actionId)
                    .setType(ActionTypeProto.valueOf(action.getType().name()))
                    .setValue(action.getValue())
                    .build();

            Instant timestamp = Instant.now();

            DeviceActionRequest deviceActionRequest = DeviceActionRequest.newBuilder()
                    .setHubId(scenario.getHubId())
                    .setScenarioName(scenario.getName())
                    .setAction(deviceActionProto)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(timestamp.getEpochSecond())
                            .setNanos(timestamp.getNano()))
                    .build();

            log.info("Attempt send device action to hub-router.");
            hubRouterGrpcClient.sendDeviceAction(deviceActionRequest);
        }
    }
}
