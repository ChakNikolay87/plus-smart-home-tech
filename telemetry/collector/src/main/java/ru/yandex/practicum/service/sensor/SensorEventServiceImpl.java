package ru.yandex.practicum.service.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.model.sensor.*;
import ru.yandex.practicum.service.EventService;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorEventServiceImpl implements EventService<SensorEvent> {
    private final KafkaClient kafkaClient;

    @Value(value = "${sensorEventTopic}")
    private String topic;

    @Override
    public void collect(SensorEvent event) {
        SensorEventAvro sensorEventAvro = mapToAvro(event);
        kafkaClient.getProducer().send(new ProducerRecord<>(topic, sensorEventAvro));
        log.info("To topic {} sent message with sensor event {}", topic, event);
    }

    private SensorEventAvro mapToAvro(SensorEvent event) {
        Object payload = switch (event) {
            case ClimateSensorEvent climateSensorEvent -> mapToClimateSensorAvro(climateSensorEvent);
            case LightSensorEvent lightSensorEvent -> mapToLightSensorAvro(lightSensorEvent);
            case MotionSensorEvent motionSensorEvent -> mapToMotionSensorAvro(motionSensorEvent);
            case SwitchSensorEvent switchSensorEvent -> mapToSwitchSensorAvro(switchSensorEvent);
            case TemperatureSensorEvent temperatureSensorEvent -> mapToTemperatureSensorAvro(temperatureSensorEvent);
            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        };

        return SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private ClimateSensorAvro mapToClimateSensorAvro(ClimateSensorEvent event) {
        return ClimateSensorAvro.newBuilder()
                .setCo2Level(event.getCo2Level())
                .setHumidity(event.getHumidity())
                .setTemperatureC(event.getTemperatureC())
                .build();
    }

    private LightSensorAvro mapToLightSensorAvro(LightSensorEvent event) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();
    }

    private MotionSensorAvro mapToMotionSensorAvro(MotionSensorEvent event) {
        return MotionSensorAvro.newBuilder()
                .setMotion(event.isMotion())
                .setLinkQuality(event.getLinkQuality())
                .setVoltage(event.getVoltage())
                .build();
    }

    private SwitchSensorAvro mapToSwitchSensorAvro(SwitchSensorEvent event) {
        return SwitchSensorAvro.newBuilder()
                .setState(event.isState())
                .build();
    }

    private TemperatureSensorAvro mapToTemperatureSensorAvro(TemperatureSensorEvent event) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(Objects.requireNonNull(event).getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();
    }
}
