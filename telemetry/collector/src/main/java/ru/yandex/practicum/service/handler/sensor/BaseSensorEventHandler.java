package ru.yandex.practicum.service.handler.sensor;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.EventKafkaProducer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

import static ru.yandex.practicum.kafka.KafkaTopic.SENSORS_EVENTS;

@Slf4j
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {
    private final EventKafkaProducer producer;

    protected BaseSensorEventHandler(EventKafkaProducer producer) {
        this.producer = producer;
    }

    @Override
    public void handle(SensorEventProto event) {
        if (!event.getPayloadCase().equals(getEventType())) {
            throw new IllegalArgumentException("Event type mismatch");
        }

        T payload = mapToAvro(event);

        SensorEventAvro avro = SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()))
                .setPayload(payload)
                .build();

        log.info("Sensor event received: {}", avro);
        producer.send(avro, avro.getHubId(), avro.getTimestamp(), SENSORS_EVENTS);
    }

    protected abstract T mapToAvro(SensorEventProto event);
}
