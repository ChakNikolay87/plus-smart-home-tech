package ru.yandex.practicum.telemetry.collector.mapper.grpc;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

public class GrpcSensorEventMapper {

    public static SensorEventAvro toAvro(SensorEventProto event) {
        String id = event.getId();
        String hubId = event.getHubId();
        long timestamp = event.getTimestamp().getSeconds() * 1000L
                + event.getTimestamp().getNanos() / 1_000_000;

        return switch (event.getPayloadCase()) {
            case CLIMATE_SENSOR_EVENT -> {
                var data = event.getClimateSensorEvent();
                ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                        .setTemperatureC(data.getTemperatureC())
                        .setHumidity(data.getHumidity())
                        .setCo2Level(data.getCo2Level())
                        .build();
                yield build(id, hubId, timestamp, payload);
            }

            case LIGHT_SENSOR_EVENT -> {
                var data = event.getLightSensorEvent();
                LightSensorAvro payload = LightSensorAvro.newBuilder()
                        .setLinkQuality(data.getLinkQuality())
                        .setLuminosity(data.getLuminosity())
                        .build();
                yield build(id, hubId, timestamp, payload);
            }

            case MOTION_SENSOR_EVENT -> {
                var data = event.getMotionSensorEvent();
                MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                        .setLinkQuality(data.getLinkQuality())
                        .setMotion(data.getMotion())
                        .setVoltage(data.getVoltage())
                        .build();
                yield build(id, hubId, timestamp, payload);
            }

            case SWITCH_SENSOR_EVENT -> {
                var data = event.getSwitchSensorEvent();
                SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                        .setState(data.getState())
                        .build();
                yield build(id, hubId, timestamp, payload);
            }

            case TEMPERATURE_SENSOR_EVENT -> {
                var data = event.getTemperatureSensorEvent();
                TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(data.getTemperatureC())
                        .setTemperatureF(data.getTemperatureF())
                        .build();
                yield build(id, hubId, timestamp, payload);
            }

            default -> throw new IllegalArgumentException(String.format("Неподдерживаемый тип события датчика: %s",
                    event.getPayloadCase()));
        };
    }

    private static SensorEventAvro build(String id, String hubId, long timestamp, Object payload) {
        return SensorEventAvro.newBuilder()
                .setId(id)
                .setHubId(hubId)
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}

