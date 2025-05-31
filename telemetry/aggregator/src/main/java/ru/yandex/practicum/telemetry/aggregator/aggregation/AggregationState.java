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

    private Optional<SensorStateAvro> getPreviousSensorState(String hubId, String sensorId) {
        return Optional.ofNullable(snapshots.get(hubId))
                .map(SensorsSnapshotAvro::getSensorsState)
                .map(sensors -> sensors.get(sensorId));
    }


    private <T> SensorsSnapshotAvro isNewState(SensorEventAvro value, Class<T> sensorClass) {
        T sensorData = sensorClass.cast(value.getPayload());
        String hubId = value.getHubId();
        String sensorId = value.getId();

        Optional<SensorStateAvro> previousStateOpt = getPreviousSensorState(hubId, sensorId);

        boolean isChanged = previousStateOpt
                .map(prev -> {
                    if (!prev.getData().getClass().equals(sensorData.getClass())) {
                        log.trace("Wrong class of sensor. Old class {}, new class {}",
                                prev.getData().getClass(), sensorData.getClass());
                        return false; // Отбросим
                    }
                    if (prev.getData().equals(sensorData)) {
                        return false;
                    }
                    return !value.getTimestamp().isBefore(prev.getTimestamp());
                })
                .orElse(true); // Если предыдущее значение отсутствует — считаем новым

        if (!isChanged) return null;

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(value.getTimestamp())
                .setData(sensorData)
                .build();

        log.trace("New snapshot value {} ", newState);
        snapshots.get(hubId).getSensorsState().put(sensorId, newState);
        return snapshots.get(hubId);
    }

}
