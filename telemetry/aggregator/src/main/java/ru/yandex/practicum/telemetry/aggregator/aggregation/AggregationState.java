package ru.yandex.practicum.telemetry.aggregator.aggregation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class AggregationState {
    //snapshots хранит снапшоты сгруппированные по хабам
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public SensorsSnapshotAvro sensorEventHandle(SensorEventAvro value) {
        Object payload = value.getPayload();
        //Проверка на случай, если по каким-то причинам возникла ошибка
        if (payload == null)
            throw new IllegalStateException("Unexpected: payload == null");
        // Если новый hub, то добавляем в snapshots
        snapshots.computeIfAbsent(value.getHubId(), k ->
                new SensorsSnapshotAvro(k, value.getTimestamp(), new HashMap<>())
        );
        String thisEventClass = value.getPayload().getClass().getSimpleName();
        return switch (thisEventClass) {
            case "ClimateSensorAvro" -> isNewState(value, ClimateSensorAvro.class);
            case "LightSensorAvro" -> isNewState(value, LightSensorAvro.class);
            case "MotionSensorAvro" -> isNewState(value, MotionSensorAvro.class);
            case "SwitchSensorAvro" -> isNewState(value, SwitchSensorAvro.class);
            case "TemperatureSensorAvro" -> isNewState(value, TemperatureSensorAvro.class);
            default -> throw new IllegalStateException("Unexpected value: " + thisEventClass);
        };
    }

    private SensorStateAvro getPreviousSensorState(String hubId, String sensorId) {
        return Optional.ofNullable(snapshots.get(hubId))
                .map(SensorsSnapshotAvro::getSensorsState)
                .map(sensorsState -> sensorsState.get(sensorId))
                .orElse(null);
    }

    private <T> SensorsSnapshotAvro isNewState(SensorEventAvro value, Class<T> sensorClass) {
        T sensorData = sensorClass.cast(value.getPayload());
        SensorStateAvro previousState = getPreviousSensorState(value.getHubId(), value.getId());

        if (previousState != null) {
            if (!previousState.getData().getClass().equals(sensorData.getClass())) {
                log.trace("Wrong class of sensor. Old class {}, new class {}",
                        previousState.getData().getClass(), sensorData.getClass());
                return null;
            }
            if (previousState.getData().equals(sensorData) ||
                    value.getTimestamp().isBefore(previousState.getTimestamp())) {
                return null;
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(value.getTimestamp())
                .setData(sensorData)
                .build();
        log.trace("New snapshot value {} ", newState);
        log.trace("snapshots.get({}): {}", value.getHubId(), snapshots.get(value.getHubId()));
        snapshots.get(value.getHubId()).getSensorsState().put(value.getId(), newState);
        return snapshots.get(value.getHubId());
    }

}
