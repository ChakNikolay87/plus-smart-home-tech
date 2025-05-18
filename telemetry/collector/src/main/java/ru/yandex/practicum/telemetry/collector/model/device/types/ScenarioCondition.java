package ru.yandex.practicum.telemetry.collector.model.device.types;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioCondition {

    private String sensorId;

    private ConditionType type;

    private ConditionOperation operation;

    private Object value;

    public boolean verifyValue() {
        //проверка, получено ли допустимое значение value
        return (value instanceof Integer) || (value instanceof Boolean);
    }

    public Class<?> getValueType() {
        //получаем класс экземпляра value
        return value == null ? null : value.getClass();
    }

}
