package ru.yandex.practicum.telemetry.collector.service.handler.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ConditionOperationProto;
import ru.yandex.practicum.grpc.telemetry.event.ConditionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.List;

@Component
public class ProtoConditionMapper {

    public static ScenarioConditionAvro condProtoToCondition(ScenarioConditionProto condProto) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder();
        builder.setSensorId(condProto.getSensorId());

        ConditionTypeProto actType = condProto.getType();
        ConditionTypeAvro avroType = ConditionTypeAvro.valueOf(actType.name());
        builder.setType(avroType);

        ConditionOperationProto protoOper = condProto.getOperation();
        ConditionOperationAvro avroOper = ConditionOperationAvro.valueOf(protoOper.name());
        builder.setOperation(avroOper);

        Object thisValue = switch (condProto.getValueCase()) {
            case BOOL_VALUE -> condProto.getBoolValue();
            case INT_VALUE -> condProto.getIntValue();
            case VALUE_NOT_SET -> null;
        };
        switch (thisValue) {
            case Integer i -> builder.setValue(i);
            case Boolean b -> builder.setValue(b);
            case null -> builder.setValue(null);
            default -> throw new IllegalStateException("Unexpected value: " + thisValue);
        }
        return builder.build();
    }

    public static List<ScenarioConditionAvro> condProtoListToCondList(List<ScenarioConditionProto> condProtoList) {
        return condProtoList.stream()
                .map(ProtoConditionMapper::condProtoToCondition)
                .toList();
    }
}
