package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.entities.Condition;
import ru.yandex.practicum.entities.Scenario;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.enums.ConditionType;
import ru.yandex.practicum.model.mappers.DeviceActionRequestMapper;
import ru.yandex.practicum.repositories.ScenarioRepository;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SnapshotHandler {

    private final ScenarioRepository scenarioRepository;

    private final Map<Class<?>, BiFunction<Condition, Object, Boolean>> isFits = Map.of(
            ClimateSensorAvro.class, this::isFitsClimateCondition,
            LightSensorAvro.class, this::isFitsLightCondition,
            MotionSensorAvro.class, this::isFitsMotionCondition,
            SwitchSensorAvro.class, this::isFitsSwitchCondition,
            TemperatureSensorAvro.class, this::isFitsTemperatureCondition
    );

    public List<DeviceActionRequest> process(SensorsSnapshotAvro snapshotAvro) {
        log.info("\nSnapshotHandler.process: received {}", snapshotAvro);
        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshotAvro.getHubId());
        return scenarios.stream()
                .filter(s -> checkScenario(s, snapshotAvro))
                .findFirst()
                .map(s -> DeviceActionRequestMapper.mapAll(s, snapshotAvro.getHubId()))
                .orElse(List.of());
    }

    private boolean checkScenario(Scenario scenario, SensorsSnapshotAvro snapshotAvro) {
        log.info("\nSnapshotHandler.checkScenario: check scenario {}", scenario);

        return scenario.getConditions().entrySet().stream()
                .allMatch(entry -> {
                    String sensorId = entry.getKey();
                    Condition condition = entry.getValue();
                    log.info("\nCondition: {}", condition);
                    SensorStateAvro state = snapshotAvro.getSensorsState().get(sensorId);
                    log.info("\nSensorStateAvro: {}", state);

                    if (state == null || state.getData() == null)
                        return false;

                    Object payload = state.getData();
                    BiFunction<Condition, Object, Boolean> inspector = isFits.get(payload.getClass());
                    if (inspector == null)
                        throw new IllegalStateException("Unexpected value class: " + payload.getClass());

                    return inspector.apply(condition, payload);
                });
    }

    private Boolean isFitsClimateCondition(Condition condition, Object payload) {
        ClimateSensorAvro sensor = (ClimateSensorAvro) payload;
        log.info("\nClimateSensorAvro: {}, check condition {}", sensor, condition);
        boolean result = switch (condition.getType()) {
            case TEMPERATURE -> isValueFits(condition, sensor.getTemperatureC());
            case HUMIDITY -> isValueFits(condition, sensor.getHumidity());
            case CO2LEVEL -> isValueFits(condition, sensor.getCo2Level());
            default -> false;
        };
        log.info("\nResult {}", result);
        return result;
    }

    private Boolean isFitsLightCondition(Condition condition, Object payload) {
        LightSensorAvro sensor = (LightSensorAvro) payload;
        if (condition.getType() == ConditionType.LUMINOSITY) {
            return isValueFits(condition, sensor.getLuminosity());
        } else {
            return true;
        }
    }

    private Boolean isFitsMotionCondition(Condition condition, Object payload) {
        MotionSensorAvro sensor = (MotionSensorAvro) payload;
        if (condition.getType() == ConditionType.MOTION) {
            return sensor.getMotion() == condition.getValueBool();
        } else {
            return false;
        }
    }

    private Boolean isFitsSwitchCondition(Condition condition, Object payload) {
        SwitchSensorAvro sensor = (SwitchSensorAvro) payload;
        if (condition.getType() == ConditionType.SWITCH) {
            return sensor.getState() == condition.getValueBool();
        } else {
            return false;
        }
    }

    private Boolean isFitsTemperatureCondition(Condition condition, Object payload) {
        TemperatureSensorAvro sensor = (TemperatureSensorAvro) payload;
        if (condition.getType() == ConditionType.TEMPERATURE) {
            return isValueFits(condition, sensor.getTemperatureC());
        } else {
            return false;
        }
    }

    private boolean isValueFits(Condition condition, int value) {
        return switch (condition.getOperation()) {
            case EQUALS -> value == condition.getValueInt();
            case GREATER_THAN -> value > condition.getValueInt();
            case LOWER_THAN -> value < condition.getValueInt();
        };
    }
}
