package ru.yandex.practicum.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceAction {
    @NotBlank
    private String sensorId;

    @NotBlank
    private ActionType type;

    @NotNull
    private Integer value;
}