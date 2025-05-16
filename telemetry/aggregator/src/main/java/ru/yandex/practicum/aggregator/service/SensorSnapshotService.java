package ru.yandex.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SensorSnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateSnapshot(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        log.debug("Обновление снапшота: hubId={}, sensorId={}, timestamp={}", hubId, sensorId, event.getTimestamp());

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, id -> {
            log.info("Создан новый снапшот для хаба: {}", hubId);
            SensorsSnapshotAvro snap = new SensorsSnapshotAvro();
            snap.setHubId(hubId);
            snap.setTimestamp(Instant.ofEpochMilli(event.getTimestamp()));
            snap.setSensorsState(new HashMap<>());
            return snap;
        });

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro oldState = sensorsState.get(sensorId);

        if (oldState != null) {
            long oldTs = oldState.getTimestamp().toEpochMilli();
            long newTs = event.getTimestamp();

            if (oldTs > newTs) {
                log.debug("Пропущено устаревшее событие от датчика {}: старый ts={}, новый ts={}", sensorId, oldTs, newTs);
                return Optional.empty();
            }

            Object newPayload = event.getPayload();
            Object oldPayload = oldState.getData();

            if (!oldPayload.getClass().equals(newPayload.getClass())) {
                log.warn("Типы payload не совпадают: old={}, new={}", oldPayload.getClass(), newPayload.getClass());
            } else if (newPayload instanceof ClimateSensorAvro newClimate &&
                    oldPayload instanceof ClimateSensorAvro oldClimate) {
                if (oldClimate.getTemperatureC() == newClimate.getTemperatureC() &&
                        oldClimate.getHumidity() == newClimate.getHumidity() &&
                        oldClimate.getCo2Level() == newClimate.getCo2Level()) {
                    log.debug("ClimateSensorAvro: данные не изменились");
                    return Optional.empty();
                }
            } else if (newPayload instanceof LightSensorAvro newLight &&
                    oldPayload instanceof LightSensorAvro oldLight) {
                if (oldLight.getLinkQuality() == newLight.getLinkQuality() &&
                        oldLight.getLuminosity() == newLight.getLuminosity()) {
                    log.debug("LightSensorAvro: данные не изменились");
                    return Optional.empty();
                }
            } else if (newPayload instanceof MotionSensorAvro newMotion &&
                    oldPayload instanceof MotionSensorAvro oldMotion) {
                if (oldMotion.getLinkQuality() == newMotion.getLinkQuality() &&
                        oldMotion.getMotion() == newMotion.getMotion() &&
                        oldMotion.getVoltage() == newMotion.getVoltage()) {
                    log.debug("MotionSensorAvro: данные не изменились");
                    return Optional.empty();
                }
            } else if (newPayload instanceof SwitchSensorAvro newSwitch &&
                    oldPayload instanceof SwitchSensorAvro oldSwitch) {
                if (oldSwitch.getState() == newSwitch.getState()) {
                    log.debug("SwitchSensorAvro: данные не изменились");
                    return Optional.empty();
                }
            } else if (newPayload instanceof TemperatureSensorAvro newTemp &&
                    oldPayload instanceof TemperatureSensorAvro oldTemp) {
                if (oldTemp.getTemperatureC() == newTemp.getTemperatureC() &&
                        oldTemp.getTemperatureF() == newTemp.getTemperatureF()) {
                    log.debug("TemperatureSensorAvro: данные не изменились");
                    return Optional.empty();
                }
            } else {
                log.warn("Необрабатываемый тип сенсора: {}", newPayload.getClass().getSimpleName());
            }
            log.debug("Обновление состояния датчика {}: старое ts={}, новое ts={}", sensorId, oldTs, newTs);

        } else {
            log.debug("Добавление нового датчика в снапшот: {}", sensorId);
        }

        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(Instant.ofEpochMilli(event.getTimestamp()));
        newState.setData(event.getPayload());
        sensorsState.put(sensorId, newState);
        snapshot.setTimestamp(Instant.ofEpochMilli(event.getTimestamp()));
        log.info("Состояние датчика {} обновлено. Обновлённый снапшот хаба {}: timestamp={}", sensorId, hubId, snapshot.getTimestamp());
        return Optional.of(snapshot);
    }
}
