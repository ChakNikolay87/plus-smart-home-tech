package ru.yandex.practicum.telemetry.collector.model.hub;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScenarioCondition {
    String sensorId;
    ConditionType type;
    ConditionOperation operation;
    Integer value;
}
