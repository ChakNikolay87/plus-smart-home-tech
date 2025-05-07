package ru.yandex.practicum.service.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.model.sensor.*;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorEventServiceImpl implements SensorEventService {
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
        Object payload;
        switch (event) {
            case ClimateSensorEvent climateSensorEvent -> payload = ClimateSensorAvro.newBuilder()
                    .setCo2Level(climateSensorEvent.getCo2Level())
                    .setHumidity(climateSensorEvent.getHumidity())
                    .setTemperatureC(climateSensorEvent.getTemperatureC())
                    .build();

            case LightSensorEvent lightSensorEvent -> payload = LightSensorAvro.newBuilder()
                    .setLinkQuality(lightSensorEvent.getLinkQuality())
                    .setLuminosity(lightSensorEvent.getLuminosity())
                    .build();

            case MotionSensorEvent motionSensorEvent -> payload = MotionSensorAvro.newBuilder()
                    .setMotion(motionSensorEvent.isMotion())
                    .setLinkQuality(motionSensorEvent.getLinkQuality())
                    .setVoltage(motionSensorEvent.getVoltage())
                    .build();

            case SwitchSensorEvent switchSensorEvent -> payload = SwitchSensorAvro.newBuilder()
                    .setState(switchSensorEvent.isState())
                    .build();

            case null, default -> {
                TemperatureSensorEvent temperatureSensorEvent = (TemperatureSensorEvent) event;
                payload = TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(Objects.requireNonNull(temperatureSensorEvent).getTemperatureC())
                        .setTemperatureF(temperatureSensorEvent.getTemperatureF())
                        .build();
            }
        }
        return SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }
}
