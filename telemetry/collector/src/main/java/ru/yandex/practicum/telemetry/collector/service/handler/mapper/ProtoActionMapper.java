package ru.yandex.practicum.telemetry.collector.service.handler.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

import java.util.List;

@Component
public class ProtoActionMapper {

    public static DeviceActionAvro actionProtoToDevAction(DeviceActionProto devProto) {
        ActionTypeProto actType = devProto.getType();
        ActionTypeAvro actionTypeAvro = ActionTypeAvro.valueOf(actType.name());
        return DeviceActionAvro.newBuilder()
                .setSensorId(devProto.getSensorId())
                .setType(actionTypeAvro)
                .setValue(devProto.getValue())
                .build();
    }

    public static List<DeviceActionAvro> actionProtoListToActionList(List<DeviceActionProto> devProtoList) {
        return devProtoList.stream()
                .map(ProtoActionMapper::actionProtoToDevAction)
                .toList();
    }
}
