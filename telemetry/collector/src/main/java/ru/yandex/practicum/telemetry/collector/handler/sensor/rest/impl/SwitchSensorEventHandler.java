package ru.yandex.practicum.telemetry.collector.handler.sensor.rest.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.handler.sensor.rest.SensorEventHandler;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.mapper.rest.SensorEventMapper;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;

@Component
@RequiredArgsConstructor
public class SwitchSensorEventHandler implements SensorEventHandler<SwitchSensorEvent> {
    private final KafkaEventProducer producer;
    private static final String TOPIC = "telemetry.sensors.v1";

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    @Override
    public void handle(SwitchSensorEvent event) {
        var avro = SensorEventMapper.toAvro(event);
        producer.send(TOPIC, event.getId(), avro);
    }
}
