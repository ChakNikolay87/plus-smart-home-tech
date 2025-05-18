package ru.yandex.practicum.telemetry.collector.service.handler.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.telemetry.collector.model.device.types.ConditionOperation;
import ru.yandex.practicum.telemetry.collector.model.device.types.ConditionType;
import ru.yandex.practicum.telemetry.collector.model.device.types.ScenarioCondition;

import java.util.List;

@Component
public class ConditionsMapper {

    public static ScenarioConditionAvro sceCondToAvro(ScenarioCondition scenario) {

        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder();
        builder.setSensorId(scenario.getSensorId());

        ConditionType thisType = scenario.getType();
        ConditionTypeAvro avroType = ConditionTypeAvro.valueOf(thisType.name());
        builder.setType(avroType);

        ConditionOperation thisOper = scenario.getOperation();
        ConditionOperationAvro avroOper = ConditionOperationAvro.valueOf(thisOper.name());
        builder.setOperation(avroOper);

        switch (scenario.getValue()) {
            case Integer i -> builder.setValue(i);
            case Boolean b -> builder.setValue(b);
            default -> builder.setValue(null);
        }

        return builder.build();
    }

    public static List<ScenarioConditionAvro> condListToAvro(List<ScenarioCondition> conditions) {
        return conditions.stream()
                .map(ConditionsMapper::sceCondToAvro)
                .toList();
    }
}
