package ru.yandex.practicum.service.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedHubEventHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        ScenarioAddedEventAvro scenarioAddedEventAvro = (ScenarioAddedEventAvro) event.getPayload();
        log.info("Received scenario added event. Scenario name {} in hub ID {}", scenarioAddedEventAvro.getName(), event.getHubId());

        Scenario scenario;
        Optional<Scenario> foundScenario = scenarioRepository.findByHubIdAndName(event.getHubId(), scenarioAddedEventAvro.getName());

        scenario = foundScenario.orElseGet(() -> Scenario.builder()
                .hubId(event.getHubId())
                .name(scenarioAddedEventAvro.getName())
                .actions(new HashMap<>())
                .conditions(new HashMap<>())
                .build());

        Map<String, Condition> conditions = scenarioAddedEventAvro.getConditions().stream()
                .collect(Collectors.toMap(ScenarioConditionAvro::getSensorId, this::mapToCondition));
        Map<String, Action> actions = scenarioAddedEventAvro.getActions().stream()
                .collect(Collectors.toMap(DeviceActionAvro::getSensorId, this::mapToAction));

        scenario.setConditions(conditions);
        scenario.setActions(actions);
        scenarioRepository.save(scenario);

        log.info("Scenario added. Scenario name {} in hub ID {}", scenarioAddedEventAvro.getName(), event.getHubId());
    }

    @Override
    public String getEventType() {
        return ScenarioAddedEventAvro.class.getName();
    }

    private Condition mapToCondition(ScenarioConditionAvro condition) {
        return Condition.builder()
                .type(ConditionType.valueOf(condition.getType().name()))
                .operation(OperationType.valueOf(condition.getOperation().name()))
                .value(mapToConditionValue(condition.getValue()))
                .build();
    }

    private Integer mapToConditionValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        throw new IllegalArgumentException("Unsupported condition type: " + value.getClass());
    }

    private Action mapToAction(DeviceActionAvro action) {
        return Action.builder()
                .type(DeviceActionType.valueOf(action.getType().name()))
                .value(action.getValue())
                .build();
    }
}
