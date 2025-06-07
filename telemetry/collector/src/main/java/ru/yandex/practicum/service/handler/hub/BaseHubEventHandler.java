package ru.yandex.practicum.service.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.EventKafkaProducer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

import static ru.yandex.practicum.kafka.KafkaTopic.HUBS_EVENTS;

@Slf4j
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {
    private final EventKafkaProducer producer;

    protected BaseHubEventHandler(EventKafkaProducer producer) {
        this.producer = producer;
    }

    @Override
    public void handle(HubEventProto event) {
        if (!event.getPayloadCase().equals(getEventType())) {
            throw new IllegalArgumentException("Event type mismatch");
        }

        T payload = mapToAvro(event);

        HubEventAvro avro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()))
                .setPayload(payload)
                .build();

        log.info("Hub event received: {}", avro);
        producer.send(avro, avro.getHubId(), avro.getTimestamp(), HUBS_EVENTS);
    }

    protected abstract T mapToAvro(HubEventProto event);
}
