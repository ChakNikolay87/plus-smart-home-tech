package ru.yandex.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.configuration.KafkaConfig;


@Slf4j
@RequiredArgsConstructor
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {

    protected final KafkaConfig.KafkaEventProducer producer;
    protected final KafkaConfig topics;

    protected abstract T mapToAvro(SensorEventProto event);

    @Override
    public void handle(SensorEventProto event) {
        T protoEvent = mapToAvro(event);
        String topic = topics.producer.getTopics().get(KafkaConfig.TopicType.SENSORS_EVENTS);

        log.info("Send event {} -> topic {}", getMessageType(), topic);
        producer.send(topic, event.getId(), protoEvent);
    }
}