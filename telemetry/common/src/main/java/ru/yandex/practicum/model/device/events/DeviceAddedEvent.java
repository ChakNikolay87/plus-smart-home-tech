package ru.yandex.practicum.model.device.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.yandex.practicum.model.device.HubEvent;
import ru.yandex.practicum.model.device.HubEventType;
import ru.yandex.practicum.model.device.types.DeviceType;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DeviceAddedEvent extends HubEvent {

    @NotBlank
    private String id;

    @NotNull
    DeviceType deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }

}
