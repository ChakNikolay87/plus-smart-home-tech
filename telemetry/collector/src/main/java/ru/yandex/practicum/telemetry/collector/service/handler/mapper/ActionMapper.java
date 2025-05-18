package ru.yandex.practicum.telemetry.collector.service.handler.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.telemetry.collector.model.device.types.ActionType;
import ru.yandex.practicum.telemetry.collector.model.device.types.DeviceAction;

import java.util.List;

@Component
public class ActionMapper {

    public static DeviceActionAvro devActToAvro(DeviceAction devAct) {
        ActionType thisType = devAct.getType();
        ActionTypeAvro actionTypeAvro = ActionTypeAvro.valueOf(thisType.name());
        return DeviceActionAvro.newBuilder()
                .setSensorId(devAct.getSensorId())
                .setType(actionTypeAvro)
                .setValue(devAct.getValue())
                .build();
    }

    public static List<DeviceActionAvro> devAtcListToAvro(List<DeviceAction> actions) {
        return actions.stream()
                .map(ActionMapper::devActToAvro)
                .toList();
    }
}
