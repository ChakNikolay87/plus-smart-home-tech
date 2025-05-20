package ru.yandex.practicum.model.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entities.Condition;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.enums.ConditionOperation;
import ru.yandex.practicum.model.enums.ConditionType;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AvroConditionMapper {

    public static Optional<Condition> condAvroToCondition(ScenarioConditionAvro condAvro) {
        Object currentValue = condAvro.getValue();
        Condition.ConditionBuilder builder = Condition.builder();
        builder.type(ConditionType.valueOf(condAvro.getType().name()));
        builder.operation(ConditionOperation.valueOf(condAvro.getOperation().name()));

        if (currentValue != null) {
            switch (currentValue) {
                case Integer i -> builder.valueInt(i);
                case Boolean b -> builder.valueBool(b);
                default -> {
                    log.warn("AvroConditionMapper.condAvroToCondition: Unsupported value type: {}", currentValue.getClass().getSimpleName());
                    return Optional.empty();
                }
            }
        }

        return Optional.of(builder.build());
    }


    public static Map<String, Condition> condAvroListToMap(List<ScenarioConditionAvro> avroList) {
        return avroList.stream()
                .map(avroCondition ->
                        AvroConditionMapper.condAvroToCondition(avroCondition)
                                .map(condition -> new AbstractMap.SimpleEntry<>(avroCondition.getSensorId(), condition))
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
