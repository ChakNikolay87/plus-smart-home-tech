package ru.yandex.practicum.telemetry.collector.handler.sensor.rest.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.handler.sensor.rest.SensorEventHandler;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.mapper.rest.SensorEventMapper;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;

@Component
@RequiredArgsConstructor
public class TemperatureSensorEventHandler implements SensorEventHandler<TemperatureSensorEvent> {

    private final KafkaEventProducer producer;
    private static final String TOPIC = "telemetry.sensors.v1";

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    public void handle(TemperatureSensorEvent event) {
        var avro = SensorEventMapper.toAvro(event);
        producer.send(TOPIC, event.getId(), avro);
    }
}

