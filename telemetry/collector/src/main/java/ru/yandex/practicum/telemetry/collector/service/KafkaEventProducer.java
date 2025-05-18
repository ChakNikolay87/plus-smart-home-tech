package ru.yandex.practicum.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.client.KafkaClient;
import ru.yandex.practicum.telemetry.collector.configuration.KafkaConfig;
import ru.yandex.practicum.model.TopicType;

import java.lang.reflect.InvocationTargetException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaConfig kafkaConfig;

    private final KafkaClient client;

    public void sendEvent(TopicType topicType, SpecificRecordBase event) {
        log.trace("\nKafkaEventProducer: event {}", event);

        String topic = kafkaConfig.getProducer().getTopics().get(topicType);
        if (topic == null) {
            throw new IllegalArgumentException("Unknown topic type: " + topicType);
        }

        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, null, event);
        log.trace("\nKafkaEventProducer: record {}, class {}", record,
                record.value() != null ? getPayloadClass(record.value()) : null);
        client.getProducer(kafkaConfig.getConfigName(), kafkaConfig.getProducer().getProperties()).send(record);
    }

    private String getPayloadClass(Object event) {
        try {
            Object payload = event.getClass().getMethod("getPayload").invoke(event);
            return payload != null ? payload.getClass().getSimpleName() : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }


}
