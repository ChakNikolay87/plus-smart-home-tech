package ru.yandex.practicum.model.device.events;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.yandex.practicum.model.device.HubEvent;
import ru.yandex.practicum.model.device.HubEventType;
import ru.yandex.practicum.model.device.types.DeviceAction;
import ru.yandex.practicum.model.device.types.ScenarioCondition;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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
