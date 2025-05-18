package ru.yandex.practicum.model.device.types;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceAction {

    private String sensorId;

    private ActionType type;

    private Integer value;

}
