package ru.yandex.practicum.telemetry.collector.handler.sensor.grpc.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.handler.sensor.grpc.GrpcSensorEventHandler;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.mapper.grpc.GrpcSensorEventMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class LightSensorGrpcHandler implements GrpcSensorEventHandler {

    private final KafkaEventProducer producer;
    private static final String TOPIC = "telemetry.sensors.v1";

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        try {
            log.info("Получено Light-событие: id={}, hubId={}, timestamp={}, payloadType={}",
                    event.getId(),
                    event.getHubId(),
                    event.getTimestamp(),
                    event.getPayloadCase()
            );
            var avro = GrpcSensorEventMapper.toAvro(event);
            log.debug("Avro-событие: {}", avro);
            producer.send(TOPIC, event.getId(), avro);
            log.info("Событие отправлено в Kafka: топик={}, ключ={}", TOPIC, event.getId());
        } catch (Exception e) {
            log.error("Ошибка обработки Light-события id={} hubId={}: {}",
                    event.getId(),
                    event.getHubId(),
                    e.getMessage(), e);
        }
    }
}
