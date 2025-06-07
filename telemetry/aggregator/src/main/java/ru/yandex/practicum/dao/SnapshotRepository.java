package ru.yandex.practicum.dao;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class SnapshotRepository {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>(); // хранение снимков датчиков по каждому хабу

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        // проверяем, есть ли снапшот для event.getHubId()
        // если снапшот есть, то достаём его, если нет, то создаём новый
        SensorsSnapshotAvro snapshot = snapshots.getOrDefault(event.getHubId(),
                SensorsSnapshotAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setSensorsState(new HashMap<>())
                        .setTimestamp(Instant.now())
                        .build());

        // проверяем, есть ли в снапшоте данные для event.getId()
        // если данные есть, то достаём их в переменную oldState
        if (snapshot.getSensorsState().containsKey(event.getId())) {
            SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());
            // проверка, если oldState.getTimestamp() произошёл позже, чем event.getTimestamp()
            // или oldState.getData() равен event.getPayload(),
            // то ничего обновлять не нужно, выходим из метода, вернув Optional.empty()
            if (oldState.getTimestamp().isAfter(event.getTimestamp()) ||
                    oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        // нужно обновить снапшот
        SensorStateAvro newSensorStateAvro = SensorStateAvro.newBuilder()
                .setData(event.getPayload())
                .setTimestamp(event.getTimestamp())
                .build();
        snapshot.setTimestamp(event.getTimestamp());
        snapshot.getSensorsState().put(event.getId(), newSensorStateAvro);
        snapshots.put(event.getHubId(), snapshot);
        return Optional.of(snapshot);
    }
}
