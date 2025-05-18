package ru.yandex.practicum.telemetry.collector.model.device.events;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.yandex.practicum.telemetry.collector.model.device.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.device.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.device.types.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.device.types.ScenarioCondition;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioAddedEvent extends HubEvent {
    @NotBlank
    private String name;

    private List<ScenarioCondition> conditions;

    private List<DeviceAction> actions;


    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}
