package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.TopicType;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.service.handler.HubProtoHandler;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseHubProtoHandler<T extends SpecificRecordBase> implements HubProtoHandler {
    protected final KafkaEventProducer producer;

    protected abstract T mapToAvro(HubEventProto event);

    @Override
    public void handle(HubEventProto event) {
        log.trace("\nBaseHubEventHandler.HubEventHandler: event {}", event);
        if (!event.getPayloadCase().equals(getMessageType())) {
            throw new IllegalArgumentException("Unknown event type: " + event);
        }

        T payload = mapToAvro(event);
        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()
        );

        HubEventAvro eventAvro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
        log.info("\nBaseHubEventHandler: eventAvro {}", eventAvro);

        producer.sendEvent(TopicType.HUBS_EVENTS, eventAvro);
    }
}
