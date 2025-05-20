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
            MotionSensorAvro.class, this::isFitsGenericCondition,
            SwitchSensorAvro.class, this::isFitsGenericCondition,
            TemperatureSensorAvro.class, this::isFitsGenericCondition
    );

    public List<DeviceActionRequest> process(SensorsSnapshotAvro snapshotAvro) {
        log.info("SnapshotHandler.process: received {}", snapshotAvro);
        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshotAvro.getHubId());
        return scenarios.stream()
                .filter(s -> checkScenario(s, snapshotAvro))
                .findFirst()
                .map(s -> DeviceActionRequestMapper.mapAll(s, snapshotAvro.getHubId()))
                .orElse(List.of());
    }

    private boolean checkScenario(Scenario scenario, SensorsSnapshotAvro snapshotAvro) {
        log.info("SnapshotHandler.checkScenario: check scenario {}", scenario);

        return scenario.getConditions().entrySet().stream()
                .allMatch(entry -> {
                    String sensorId = entry.getKey();
                    Condition condition = entry.getValue();
                    log.info("Condition: {}", condition);
                    SensorStateAvro state = snapshotAvro.getSensorsState().get(sensorId);
                    log.info("SensorStateAvro: {}", state);

                    if (state == null || state.getData() == null) {
                        return false;
                    }
                    Object payload = state.getData();
                    BiFunction<Condition, Object, Boolean> inspector = isFits.get(payload.getClass());
                    if (inspector == null)
                        throw new IllegalStateException("Unexpected value class: " + payload.getClass());

                    return inspector.apply(condition, payload);
                });
    }

    private Boolean isFitsClimateCondition(Condition condition, Object payload) {
        ClimateSensorAvro sensor = (ClimateSensorAvro) payload;
        log.info("ClimateSensorAvro: {}, check condition {}", sensor, condition);

        Object value = switch (condition.getType()) {
            case TEMPERATURE -> sensor.getTemperatureC();
            case HUMIDITY -> sensor.getHumidity();
            case CO2LEVEL -> sensor.getCo2Level();
            default -> null;
        };

        if (value == null) return false;

        boolean result = condition.getType().matches(condition, value);
        log.info("Result {}", result);
        return result;
    }

    private Boolean isFitsLightCondition(Condition condition, Object payload) {
        LightSensorAvro sensor = (LightSensorAvro) payload;
        if (condition.getType() != ConditionType.LUMINOSITY) {
            return true;
        }
        return condition.getType().matches(condition, sensor.getLuminosity());
    }

    private Boolean isFitsGenericCondition(Condition condition, Object payload) {
        Object sensorValue;

        if (payload instanceof MotionSensorAvro && condition.getType() == ConditionType.MOTION) {
            sensorValue = ((MotionSensorAvro) payload).getMotion();
        } else if (payload instanceof SwitchSensorAvro && condition.getType() == ConditionType.SWITCH) {
            sensorValue = ((SwitchSensorAvro) payload).getState();
        } else if (payload instanceof TemperatureSensorAvro && condition.getType() == ConditionType.TEMPERATURE) {
            sensorValue = ((TemperatureSensorAvro) payload).getTemperatureC();
        } else {
            return false;
        }

        return condition.getType().matches(condition, sensorValue);
    }
}
