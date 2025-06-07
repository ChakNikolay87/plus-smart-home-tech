package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaConfig;
import ru.yandex.practicum.configuration.KafkaTopic;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.handler.snapshot.SnapshotHandler;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Component
public class SnapshotProcessor {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final EnumMap<KafkaTopic, String> topics;
    private final SnapshotHandler snapshotHandler;

    public SnapshotProcessor(KafkaConfig kafkaConfig, SnapshotHandler snapshotHandler) {
        consumer = new KafkaConsumer<>(kafkaConfig.getSnapshotConsumerProps());
        topics = kafkaConfig.getTopics();
        this.snapshotHandler = snapshotHandler;
    }

    public void start() {
        consumer.subscribe(List.of(topics.get(KafkaTopic.SENSORS_SNAPSHOTS)));

        // регистрируем хук при завершении JVM
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            // Цикл обработки событий
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (records.isEmpty()) continue;

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    log.info("Received snapshot: topic = {}, partition = {}, offset = {}, value = {}",
                            record.topic(), record.partition(), record.offset(), record.value());
                    // обработка событий от сенсоров
                    snapshotHandler.handle(record.value());
                    log.info("Snapshot has been processed.");
                }

                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Error during offset fixing. Offset: {}", offsets, exception);
                    }
                });
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер в блоке finally
        } catch (Exception e) {
            log.error("Error during processing of events from sensors", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Closing the consumer");
                consumer.close();
            }
        }

    }
}
