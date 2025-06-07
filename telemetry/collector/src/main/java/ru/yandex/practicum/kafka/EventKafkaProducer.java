package ru.yandex.practicum.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventKafkaProducer {
    private final KafkaTemplate<String, SpecificRecordBase> producer;
    private final KafkaTopicConfig topicConfig;

    public void send(SpecificRecordBase event, String hubId, Instant timestamp, KafkaTopic topic) {
        ProducerRecord<String, SpecificRecordBase> record =
                new ProducerRecord<>(
                        topicConfig.getTopics().get(topic),
                        null,
                        timestamp.toEpochMilli(),
                        hubId,
                        event);

        log.info("Sending event {} at hub ID {} to topic {}", event.getClass().getSimpleName(), hubId, topic);
        CompletableFuture<SendResult<String, SpecificRecordBase>> future = producer.send(record);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event {} at hub ID {} has been sent to the topic {}", event.getClass().getSimpleName(), hubId, topic);
            } else {
                log.error("Error sending the event {}, error: {}", event.getClass().getSimpleName(), ex.getMessage());
            }
        });
    }

    @PreDestroy
    public void close() {
        log.info("Shutting down producer");
        producer.flush();
        producer.destroy();
    }
}
