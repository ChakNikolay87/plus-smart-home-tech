package ru.yandex.practicum.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.configuration.KafkaConfig;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

@Slf4j
@AllArgsConstructor
public abstract class BaseHubEventHandler <T extends SpecificRecordBase> implements HubEventHandler{

    protected final KafkaConfig.KafkaEventProducer producer;
    protected final KafkaConfig topics;

    protected abstract T mapToAvro(HubEventProto event);

    @Override
    public void handle(HubEventProto event) {
        T protoEvent = mapToAvro(event);
        String topic = topics.producer.getTopics().get(KafkaConfig.TopicType.HUBS_EVENTS);
        log.info("Send event {} -> topic {}", getMessageType(), topic);
        producer.send(topic, event.getHubId(), protoEvent);
    }
}
