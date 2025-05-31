package ru.yandex.practicum.model.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.entities.Action;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.model.enums.ActionType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AvroActionMapper {

    public static Action actionAvroToAction(DeviceActionAvro devAvro) {
        return Action.builder()
                .type(ActionType.valueOf(devAvro.getType().name()))
                .value(devAvro.getValue())
                .build();
    }

    public static Map<String, Action> actionAvroListToMap(List<DeviceActionAvro> avroList) {
        return avroList.stream()
                .collect(Collectors.toMap(
                        DeviceActionAvro::getSensorId,
                        AvroActionMapper::actionAvroToAction
                ));
    }

}
