package ru.yandex.practicum.telemetry.collector.service.handler.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ConditionOperationProto;
import ru.yandex.practicum.grpc.telemetry.event.ConditionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.model.device.types.ConditionOperation;
import ru.yandex.practicum.model.device.types.ConditionType;
import ru.yandex.practicum.model.device.types.ScenarioCondition;

import java.util.List;

@Component
public class ProtoConditionMapper {

    public static ScenarioCondition condProtoToCondition(ScenarioConditionProto condProto) {
        ConditionTypeProto actType = condProto.getType();
        ConditionType thisType = ConditionType.valueOf(actType.name());

        ConditionOperationProto condOperType = condProto.getOperation();
        ConditionOperation thisCond = ConditionOperation.valueOf(condOperType.name());

        Object thisValue = switch (condProto.getValueCase()) {
            case BOOL_VALUE -> condProto.getBoolValue();
            case INT_VALUE -> condProto.getIntValue();
            case VALUE_NOT_SET -> null;
        };
        return ScenarioCondition.builder()
                .sensorId(condProto.getSensorId())
                .operation(thisCond)
                .type(thisType)
                .value(thisValue)
                .build();
    }

    public static List<ScenarioCondition> condProtoListToCondList(List<ScenarioConditionProto> condProtoList) {
        return condProtoList.stream()
                .map(ProtoConditionMapper::condProtoToCondition)
                .toList();
    }
}
