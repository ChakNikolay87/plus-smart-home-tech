package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.entities.Action;
import ru.yandex.practicum.entities.Condition;
import ru.yandex.practicum.entities.Scenario;
import ru.yandex.practicum.entities.Sensor;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.mappers.AvroActionMapper;
import ru.yandex.practicum.model.mappers.AvroConditionMapper;
import ru.yandex.practicum.repositories.ScenarioRepository;
import ru.yandex.practicum.repositories.SensorRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class HubHandler {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    private final Map<Class<?>, Consumer<HubEventAvro>> hubHandlers = Map.of(
            DeviceAddedEventAvro.class, this::addDevice,
            DeviceRemovedEventAvro.class, this::removeDevice,
            ScenarioAddedEventAvro.class, this::addScenario,
            ScenarioRemovedEventAvro.class, this::removeScenario
    );

    public void habEventHandle(HubEventAvro value) {
        log.info("HubHandler.habEventHandle: accepted {}", value);
        Object payload = value.getPayload();
        if (payload == null)
            throw new IllegalStateException("HubHandler: unexpected: payload == null");

        Consumer<HubEventAvro> handler = hubHandlers.get(payload.getClass());
        if (handler == null)
            throw new IllegalStateException("habEventHandle: Unexpected value payload.getClass: " + payload.getClass());

        handler.accept(value);
    }

    private void addDevice(HubEventAvro value) {
        DeviceAddedEventAvro payload = (DeviceAddedEventAvro) value.getPayload();
        Sensor sensor = Sensor.builder()
                .id(payload.getId())
                .hubId(value.getHubId())
                .build();
        //Поскольку у нас нет события типа DeviceUpdate пока считаем, что добавление сенсора с имеющимся id
        // это переключение его на новый hub
        log.info("addDevice: write to Base sensor {}", sensor);
        sensorRepository.save(sensor);
    }

    private void removeDevice(HubEventAvro value) {
        log.info("Hub {} for removing", value);
        DeviceRemovedEventAvro payload = (DeviceRemovedEventAvro) value.getPayload();
        String sensorId = payload.getId();
        Sensor sensor = sensorRepository.findByIdAndHubId(sensorId, value.getHubId()).orElse(null);
        if (sensor == null) {
            log.info("Sensor id {}, hubId {} not exists, removing is impossible.", sensorId, value.getHubId());
        } else {
            List<Scenario> scenarios = scenarioRepository.findByHubId(sensor.getHubId());
            for (Scenario scenario : scenarios) {
                scenario.getActions().remove(sensorId);
                scenario.getConditions().remove(sensorId);
            }
            scenarioRepository.saveAll(scenarios);

            // Удаляем пустые сценарии
            List<Scenario> emptyScenarios = scenarios.stream()
                    .filter(s -> s.getActions().isEmpty() && s.getConditions().isEmpty())
                    .toList();
            scenarioRepository.deleteAll(emptyScenarios);
            sensorRepository.delete(sensor);
        }
    }

    private void addScenario(HubEventAvro value) {
        log.info("Scene {} for adding", value);
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) value.getPayload();

        Map<String, Action> actions = AvroActionMapper.actionAvroListToMap(payload.getActions());
        Map<String, Condition> conditions = AvroConditionMapper.condAvroListToMap(payload.getConditions());

        Scenario scenario = Scenario.builder()
                .hubId(value.getHubId()) // hubId из HubEventAvro
                .name(payload.getName()) // name из ScenarioAddedEventAvro
                .actions(actions)
                .conditions(conditions)
                .build();

        scenarioRepository.save(scenario);
    }

    private void removeScenario(HubEventAvro value) {
        log.info("Scene {} for removing", value);
        ScenarioRemovedEventAvro payload = (ScenarioRemovedEventAvro) value.getPayload();
        Scenario scenario = scenarioRepository.findByHubIdAndName(value.getHubId(), payload.getName()).orElse(null);
        if (scenario == null) {
            log.info("Scenario hubId {}, name {} not exists, removing is impossible.", value.getHubId(), payload.getName());
        } else {
            scenarioRepository.delete(scenario);
        }
    }

}
