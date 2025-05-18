package ru.yandex.practicum.telemetry.collector.service.handler.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.telemetry.collector.model.device.types.ActionType;
import ru.yandex.practicum.telemetry.collector.model.device.types.DeviceAction;

import java.util.List;

@Component
public class ProtoActionMapper {

    public static DeviceAction actionProtoToDevAction(DeviceActionProto devProto) {
        ActionTypeProto actType = devProto.getType();
        ActionType thisType = ActionType.valueOf(actType.name());
        return DeviceAction.builder()
                .sensorId(devProto.getSensorId())
                .type(thisType)
                .value(devProto.getValue())
                .build();
    }

    public static List<DeviceAction> actionProtoListToActionList(List<DeviceActionProto> devProtoList) {
        return devProtoList.stream()
                .map(ProtoActionMapper::actionProtoToDevAction)
                .toList();
    }
}
