package ru.yandex.practicum.telemetry.collector.handler.sensor.grpc.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.handler.sensor.grpc.GrpcSensorEventHandler;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.mapper.grpc.GrpcSensorEventMapper;

@Component
@RequiredArgsConstructor
public class SwitchSensorGrpcHandler implements GrpcSensorEventHandler {

    private final KafkaEventProducer producer;
    private static final String TOPIC = "telemetry.sensors.v1";

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        var avro = GrpcSensorEventMapper.toAvro(event);
        producer.send(TOPIC, event.getId(), avro);
    }
}