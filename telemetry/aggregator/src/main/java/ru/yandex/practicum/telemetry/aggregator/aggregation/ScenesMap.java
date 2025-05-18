package ru.yandex.practicum.telemetry.aggregator.aggregation;

import lombok.Getter;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ScenesMap {
//key = ScenarioAddedEventAvro.name
    private final Map<String, ScenarioAddedEventAvro> innerMap = new HashMap<>();

    public void addScene(HubEventAvro value) {
        ScenarioAddedEventAvro scenario = (ScenarioAddedEventAvro) value.getPayload();
        innerMap.put(scenario.getName(), scenario);
    }

    public boolean removeScene(HubEventAvro value) {
        return (innerMap.remove(value.getHubId()) != null);
    }

}
