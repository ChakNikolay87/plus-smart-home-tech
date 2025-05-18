package ru.yandex.practicum.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.model.sensor.LightSensorEvent;
import ru.yandex.practicum.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.model.sensor.TemperatureSensorEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class SensorEventMapper {

    private final Map<Class<?>, Function<SensorEventAvro, SensorEvent>> mappers = new HashMap<>();

    public SensorEventMapper() {
        mappers.put(ClimateSensorAvro.class, this::mapClimateSensorEvent);
        mappers.put(LightSensorAvro.class, this::mapLightSensorEvent);
        mappers.put(MotionSensorAvro.class, this::mapMotionSensorEvent);
        mappers.put(SwitchSensorAvro.class, this::mapSwitchSensorEvent);
        mappers.put(TemperatureSensorAvro.class, this::mapTemperatureSensorEvent);
    }

    public SensorEvent mapToDomain(SensorEventAvro avro) {
        Object payload = avro.getPayload();

        if (mappers.containsKey(payload.getClass())) {
            return mappers.get(payload.getClass()).apply(avro);
        } else {
            log.trace("Unknown event type received: id {}, hubId {}, payloadClass {}",
                    avro.getId(), avro.getHubId(), payload.getClass());
            throw new IllegalArgumentException("Unknown event type: " + payload.getClass());
        }
    }

    private ClimateSensorEvent mapClimateSensorEvent(SensorEventAvro avro) {
        ClimateSensorAvro payload = (ClimateSensorAvro) avro.getPayload();
        ClimateSensorEvent event = new ClimateSensorEvent();
        event.setId(avro.getId());
        event.setHubId(avro.getHubId());
        event.setTimestamp(avro.getTimestamp());
        event.setTemperatureC(payload.getTemperatureC());
        event.setHumidity(payload.getHumidity());
        event.setCo2Level(payload.getCo2Level());
        return event;
    }

    private LightSensorEvent mapLightSensorEvent(SensorEventAvro avro) {
        LightSensorAvro payload = (LightSensorAvro) avro.getPayload();
        LightSensorEvent event = new LightSensorEvent();
        event.setId(avro.getId());
        event.setHubId(avro.getHubId());
        event.setTimestamp(avro.getTimestamp());
        event.setLinkQuality(payload.getLinkQuality());
        event.setLuminosity(payload.getLuminosity());
        return event;
    }

    private MotionSensorEvent mapMotionSensorEvent(SensorEventAvro avro) {
        MotionSensorAvro payload = (MotionSensorAvro) avro.getPayload();
        MotionSensorEvent event = new MotionSensorEvent();
        event.setId(avro.getId());
        event.setHubId(avro.getHubId());
        event.setTimestamp(avro.getTimestamp());
        event.setLinkQuality(payload.getLinkQuality());
        event.setMotion(payload.getMotion());
        event.setVoltage(payload.getVoltage());
        return event;
    }

    private SwitchSensorEvent mapSwitchSensorEvent(SensorEventAvro avro) {
        SwitchSensorAvro payload = (SwitchSensorAvro) avro.getPayload();
        SwitchSensorEvent event = new SwitchSensorEvent();
        event.setId(avro.getId());
        event.setHubId(avro.getHubId());
        event.setTimestamp(avro.getTimestamp());
        event.setState(payload.getState());
        return event;
    }

    private TemperatureSensorEvent mapTemperatureSensorEvent(SensorEventAvro avro) {
        TemperatureSensorAvro payload = (TemperatureSensorAvro) avro.getPayload();
        TemperatureSensorEvent event = new TemperatureSensorEvent();
        event.setId(payload.getId());
        event.setHubId(payload.getHubId());
        event.setTimestamp(payload.getTimestamp());
        event.setTemperatureC(payload.getTemperatureC());
        event.setTemperatureF(payload.getTemperatureF());
        return event;
    }
}
