package ru.yandex.practicum.telemetry.collector.model.device.events;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.telemetry.collector.model.device.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.device.HubEventType;

@Getter
@Setter
@ToString
public class DeviceRemovedEvent extends HubEvent {

    @NotBlank
    private String removedId;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }
}
