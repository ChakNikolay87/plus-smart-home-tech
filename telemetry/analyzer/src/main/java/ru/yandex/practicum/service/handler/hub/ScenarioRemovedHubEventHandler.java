package ru.yandex.practicum.service.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedHubEventHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        ScenarioRemovedEventAvro scenarioRemovedEventAvro = (ScenarioRemovedEventAvro) event.getPayload();
        log.info("Received scenario removed event. Scenario name {} in hub ID {}", scenarioRemovedEventAvro.getName(), event.getHubId());

        Optional<Scenario> foundScenario = scenarioRepository.findByHubIdAndName(event.getHubId(), scenarioRemovedEventAvro.getName());

        if (foundScenario.isEmpty()) {
            log.info("Scenario not found. Scenario name {} in hub ID {}", event.getHubId(), event.getHubId());
            return;
        }

        scenarioRepository.delete(foundScenario.get());
        log.info("Scenario removed. Scenario name {} in hub ID {}", scenarioRemovedEventAvro.getName(), event.getHubId());
    }

    @Override
    public String getEventType() {
        return ScenarioRemovedEventAvro.class.getName();
    }
}
