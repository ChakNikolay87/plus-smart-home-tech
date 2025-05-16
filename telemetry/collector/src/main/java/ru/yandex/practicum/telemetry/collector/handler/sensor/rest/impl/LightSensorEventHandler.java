package ru.yandex.practicum.telemetry.collector.handler.sensor.rest.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.handler.sensor.rest.SensorEventHandler;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.mapper.rest.SensorEventMapper;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType;

@Component
@RequiredArgsConstructor
public class LightSensorEventHandler implements SensorEventHandler<LightSensorEvent> {
    private final KafkaEventProducer producer;
    private static final String TOPIC = "telemetry.sensors.v1";

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    public void handle(LightSensorEvent event) {
        var avro = SensorEventMapper.toAvro(event);
        producer.send(TOPIC, event.getId(), avro);
    }
}
