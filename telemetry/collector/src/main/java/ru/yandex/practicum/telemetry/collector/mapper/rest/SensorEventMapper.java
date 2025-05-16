package ru.yandex.practicum.telemetry.collector.mapper.rest;

import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.model.sensor.*;

public class SensorEventMapper {

    public static SensorEventAvro toAvro(SensorEvent event) {
        long timestamp = event.getTimestamp().toEpochMilli();
        String id = event.getId();
        String hubId = event.getHubId();

        if (event instanceof ClimateSensorEvent climate) {
            ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                    .setTemperatureC(climate.getTemperatureC())
                    .setHumidity(climate.getHumidity())
                    .setCo2Level(climate.getCo2Level())
                    .build();
            return buildAvro(id, hubId, timestamp, payload);
        }

        if (event instanceof LightSensorEvent light) {
            LightSensorAvro payload = LightSensorAvro.newBuilder()
                    .setLinkQuality(light.getLinkQuality())
                    .setLuminosity(light.getLuminosity())
                    .build();
            return buildAvro(id, hubId, timestamp, payload);
        }

        if (event instanceof MotionSensorEvent motion) {
            MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                    .setLinkQuality(motion.getLinkQuality())
                    .setMotion(motion.isMotion())
                    .setVoltage(motion.getVoltage())
                    .build();
            return buildAvro(id, hubId, timestamp, payload);
        }

        if (event instanceof SwitchSensorEvent sw) {
            SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                    .setState(sw.isState())
                    .build();
            return buildAvro(id, hubId, timestamp, payload);
        }

        if (event instanceof TemperatureSensorEvent temp) {
            TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(temp.getTemperatureC())
                    .setTemperatureF(temp.getTemperatureF())
                    .build();
            return buildAvro(id, hubId, timestamp, payload);
        }

        throw new IllegalArgumentException(String.format("Неподдерживаемый тип события датчика: %s",
                event.getClass().getSimpleName()));
    }

    private static SensorEventAvro buildAvro(String id, String hubId, long timestamp, Object payload) {
        return SensorEventAvro.newBuilder()
                .setId(id)
                .setHubId(hubId)
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}
