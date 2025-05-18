package ru.yandex.practicum.model.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entities.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.enums.ConditionOperation;
import ru.yandex.practicum.model.enums.ConditionType;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AvroConditionMapper {

    public static Condition condAvroToCondition(ScenarioConditionAvro condAvro) {
        Object currentValue = condAvro.getValue();
        Condition.ConditionBuilder builder = Condition.builder();
        builder.type(ConditionType.valueOf(condAvro.getType().name()));
        builder.operation(ConditionOperation.valueOf(condAvro.getOperation().name()));
        if (currentValue != null) {
            switch (currentValue) {
                case Integer i -> builder.valueInt(i);
                case Boolean b -> builder.valueBool(b);
                default -> {
                    log.trace("AvroConditionMapper.condAvroToCondition: Unexpected value: {}", currentValue.getClass().getSimpleName());
                    return null;
                }
            }
        }

        return builder.build();
    }

    public static Map<String, Condition> condAvroListToMap(List<ScenarioConditionAvro> avroList) {
        return avroList.stream()
                .map(avroCondition -> {
                    // Преобразуем avro в Condition, пропуская если результат null
                    Condition condition = AvroConditionMapper.condAvroToCondition(avroCondition);
                    return condition != null ? new AbstractMap.SimpleEntry<>(avroCondition.getSensorId(), condition) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
