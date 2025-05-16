package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.client.HubRouterGrpcClient;
import ru.yandex.practicum.telemetry.analyzer.kafka.KafkaSnapshotConsumer;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioAction;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SnapshotProcessor {

    ScenarioRepository scenarioRepository;
    ScenarioConditionRepository scenarioConditionRepository;
    ScenarioActionRepository scenarioActionRepository;
    HubRouterGrpcClient client;
    KafkaSnapshotConsumer consumer;

    public void start() {
            log.info("Инициализация подписки на топик telemetry.snapshots.v1");
            consumer.subscribe(List.of("telemetry.snapshots.v1"));
       try {
            while (true) {
                Thread.sleep(2000);
                var records = consumer.poll(Duration.ofMillis(100));
                if (!records.isEmpty()) {
                    for (var record : records) {
                        var snapshot = record.value();
                        log.debug("Обработка снепшота: {}", snapshot);
                        try {
                            process(snapshot);
                        } catch (Exception e) {
                            log.error("Ошибка при обработке снепшота: {}", snapshot, e);
                        }
                    }
                    try {
                        consumer.commit();
                    } catch (Exception e) {
                        log.error("Ошибка при коммите смещений", e);
                    }
                }
            }
        } catch (WakeupException e) {
            log.info("Получен сигнал завершения (WakeupException)");
        } catch (Exception e) {
            log.error("Критическая ошибка во время обработки событий", e);
        } finally {
            try {
                log.info("Закрытие консьюмера");
                consumer.close();
                log.info("Консьюмер успешно закрыт");
            } catch (Exception e) {
                log.error("Ошибка при закрытии консьюмера", e);
            }
        }
    }

    public void process(SensorsSnapshotAvro snapshot) {
        log.warn("Сработал метод process() для снапшота: {}", snapshot);
        String hubId = snapshot.getHubId();
        Instant ts = snapshot.getTimestamp();

        log.info("Обработка снапшота для хаба {} в {}", hubId, ts);
        log.debug("Сенсоры в снапшоте: {}", snapshot.getSensorsState().keySet());
        snapshot.getSensorsState()
                .forEach((k, v) -> log.debug("  [{}] → {}", k, v.getData().getClass()));

        var scenarios = scenarioRepository.findAllByHubId(hubId);
        log.debug("Найдено {} сценариев для хаба {}", scenarios.size(), hubId);

        for (Scenario scenario : scenarios) {
            log.debug("Проверка сценария: {}", scenario.getName());
            var conditions = scenarioConditionRepository.findAllByScenarioId(scenario.getId());

            log.debug("Условия сценария ({}): {}", conditions.size(), conditions);
            if (isSatisfied(conditions, snapshot)) {
                log.info("Сценарий '{}' активирован", scenario.getName());
                var actions = scenarioActionRepository.findAllByScenarioId(scenario.getId());

                for (ScenarioAction action : actions) {
                    String sensorId = action.getSensor().getId();
                    String type = action.getAction().getType();
                    Integer value = action.getAction().getValue();
                    log.info("Отправка команды: sensorId={}, type={}, value={}", sensorId, type, value);
                    log.warn("Условия выполнены, отправляем команду: {}", action);
                    client.sendDeviceAction(hubId, scenario.getName(), sensorId, value, type);
                }
            } else {
                log.warn("Условия не выполнены: {}", scenario.getName());
            }
        }
    }

    private boolean isSatisfied(List<ScenarioCondition> conditions, SensorsSnapshotAvro snapshot) {
        for (ScenarioCondition condition : conditions) {
            var sensorId = condition.getSensor().getId();
            var state = snapshot.getSensorsState().get(sensorId);

            if (state == null) {
                log.warn("Данные по сенсору '{}' отсутствуют в снапшоте", sensorId);
                log.warn("Сенсоры из снапшота: {}", snapshot.getSensorsState().keySet());
                return false;
            }

            log.debug("Найден сенсор '{}': {}", sensorId, state);
            boolean conditionMet = evaluateCondition(condition.getCondition(), state.getData());
            log.debug("Условие по сенсору {}: {}", sensorId, conditionMet ? "выполнено" : "не выполнено");

            if (!conditionMet) return false;
        }
        return true;
    }

    private boolean evaluateCondition(Condition condition, Object data) {
        String type = condition.getType();

        if (data == null) {
            log.warn("Данные сенсора отсутствуют (data == null) для условия: {}", condition);
            return false;
        }

        log.debug("Тип данных из снапшота: {}", data.getClass().getName());
        String operation = condition.getOperation();
        Integer expectedValue = condition.getValue();

        if (expectedValue == null) {
            log.error("Expected value in condition is null: {}", condition);
            return false;
        }

        log.debug("Проверка условия: type={}, operation={}, expected={}", type, operation, expectedValue);
        switch (type) {
            case "LUMINOSITY" -> {
                log.debug("Проверка типа LUMINOSITY для data={}", data.getClass().getName());
                if (data instanceof LightSensorAvro light) {
                    return compareAndLog("LUMINOSITY", light.getLuminosity(), expectedValue, operation);
                } else {
                    log.warn("Ожидался LightSensorAvro, но пришёл {}", data.getClass().getName());
                    return false;
                }
            }
            case "TEMPERATURE" -> {
                log.debug("Проверка типа TEMPERATURE для data={}", data.getClass().getName());
                if (data instanceof ClimateSensorAvro climate) {
                    return compareAndLog("TEMPERATURE (climate)", climate.getTemperatureC(), expectedValue,
                            operation);
                } else if (data instanceof TemperatureSensorAvro temp) {
                    boolean resultC = compareAndLog("TEMPERATURE (temp, °C)", temp.getTemperatureC(),
                            expectedValue, operation);
                    boolean resultF = compareAndLog("TEMPERATURE (temp, °F)", temp.getTemperatureF(),
                            expectedValue, operation);
                    return resultC || resultF;
                } else {
                    log.warn("Ожидался ClimateSensorAvro или TemperatureSensorAvro, но пришёл {}",
                            data.getClass().getName());
                    return false;
                }
            }
            case "CO2LEVEL" -> {
                log.debug("Проверка типа CO2LEVEL для data={}", data.getClass().getName());
                if (data instanceof ClimateSensorAvro climate) {
                    return compareAndLog("CO2LEVEL", climate.getCo2Level(), expectedValue, operation);
                } else {
                    log.warn("Ожидался ClimateSensorAvro, но пришёл {}", data.getClass().getName());
                    return false;
                }
            }
            case "HUMIDITY" -> {
                log.debug("Проверка типа HUMIDITY для data={}", data.getClass().getName());
                if (data instanceof ClimateSensorAvro climate) {
                    return compareAndLog("HUMIDITY", climate.getHumidity(), expectedValue, operation);
                } else {
                    log.warn("Ожидался ClimateSensorAvro, но пришёл {}", data.getClass().getName());
                    return false;
                }
            }
            case "MOTION" -> {
                log.debug("Проверка типа MOTION для data={}", data.getClass().getName());
                if (data instanceof MotionSensorAvro motion) {
                    return compareAndLog("MOTION", motion.getMotion() ? 1 : 0, expectedValue, operation);
                } else {
                    log.warn("Ожидался MotionSensorAvro, но пришёл {}", data.getClass().getName());
                    return false;
                }
            }
            case "SWITCH" -> {
                log.debug("Проверка типа SWITCH для data={}", data.getClass().getName());
                if (data instanceof SwitchSensorAvro sw) {
                    return compareAndLog("SWITCH", sw.getState() ? 1 : 0, expectedValue, operation);
                } else {
                    log.warn("Ожидался SwitchSensorAvro, но пришёл {}", data.getClass().getName());
                    return false;
                }
            }
            default -> {
                log.warn("Неизвестный тип условия: {}", type);
                return false;
            }
        }

    }

    private boolean compare(int actual, int expected, String operation) {
        return switch (operation) {
            case "EQUALS" -> actual == expected;
            case "GREATER_THAN" -> actual > expected;
            case "LOWER_THAN" -> actual < expected;
            default -> false;
        };
    }

    private boolean compareAndLog(String label, int actual, int expected, String operation) {
        boolean result = compare(actual, expected, operation);
        log.debug("{}: {} {} {} → {}", label, actual, expected, operation, result ? "выполнено" : "не выполнено");
        return result;
    }
}

