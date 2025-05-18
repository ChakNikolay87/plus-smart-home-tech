package ru.yandex.practicum.telemetry.aggregator.aggregation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class AggregationState {
    //scenes хранит сценарии, связанные с хабом
    private final Map<String, ScenesMap> scenes = new ConcurrentHashMap<>();
    //snapshots хранит снапшоты сгруппированные по хабам
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    private final Map<Class<?>, Function<HubEventAvro, Void>> hubHandlers = Map.of(
            DeviceAddedEventAvro.class, this::addHub,
            DeviceRemovedEventAvro.class, this::removeHub,
            ScenarioAddedEventAvro.class, this::addScenario,
            ScenarioRemovedEventAvro.class, this::removeScenario
    );

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

    //Методы обработки HubEventAvro
    public void habEventHandle(HubEventAvro value) {
        Object payload = value.getPayload();
        if (payload == null)
            throw new IllegalStateException("Unexpected: payload == null");

        Function<HubEventAvro, Void> handler = hubHandlers.get(payload.getClass());
        if (handler == null)
            throw new IllegalStateException("habEventHandle: Unexpected value payload.getClass: " + payload.getClass());

        handler.apply(value);
    }

    private Void addHub(HubEventAvro value) {
        if (!scenes.containsKey(value.getHubId())) {
            scenes.put(value.getHubId(), new ScenesMap());
            snapshots.put(value.getHubId(), null);
        } else throw new IllegalArgumentException("Hub id " + value.getHubId() + " is already exists.");
        log.trace("Hub {} was created", value.getHubId());
        return null;
    }

    private Void removeHub(HubEventAvro value) {
        if (scenes.remove(value.getHubId()) != null) {
            snapshots.remove(value.getHubId());
            log.trace("Hub {} was removed", value.getHubId());
        } else {
            log.trace("Hub {} was not existed", value.getHubId());
        }
        return null;
    }

    private Void addScenario(HubEventAvro value) {
        if (!scenes.containsKey(value.getHubId()))
            throw new IllegalArgumentException("Hub id " + value.getHubId() + " was not added.");
        scenes.get(value.getHubId()).addScene(value);
        log.trace("Scene {} was added", value);
        return null;
    }

    private Void removeScenario(HubEventAvro value) {
        if (!scenes.containsKey(value.getHubId()))
            throw new IllegalArgumentException("Hub id " + value.getHubId() + " was not added.");
        if (scenes.get(value.getHubId()).removeScene(value)) {
            log.trace("Scenario was not exist");
        }
        return null;
    }

}
