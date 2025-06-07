package ru.yandex.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaConfig;
import ru.yandex.practicum.configuration.KafkaTopic;
import ru.yandex.practicum.dao.SnapshotRepository;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.*;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
public class AggregationStarter {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private static final int COUNT_FIX_OFFSETS = 10; // кол-во офсетов для фиксации за раз
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final EnumMap<KafkaTopic, String> topics;
    private final SnapshotRepository repository;

    public AggregationStarter(KafkaConfig kafkaConfig, SnapshotRepository snapshotRepository) {
        topics = kafkaConfig.getTopics();
        producer = new KafkaProducer<>(kafkaConfig.getProducerProps());
        consumer = new KafkaConsumer<>(kafkaConfig.getConsumerProps());
        repository = snapshotRepository;
    }

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */
    public void start() {
        log.info("Starting aggregator...");
        try {
            // подписка на топик
            consumer.subscribe(List.of(topics.get(KafkaTopic.SENSORS_EVENTS)));

            // регистрируем хук при завершении JVM
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            // Цикл обработки событий
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (records.isEmpty()) continue;

                int count = 0;
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.info("Received record: topic = {}, partition = {}, offset = {}, value = {}",
                            record.topic(), record.partition(), record.offset(), record.value());
                    Optional<SensorsSnapshotAvro> sensorsSnapshotAvroOpt = repository.updateState(record.value());
                    if (sensorsSnapshotAvroOpt.isPresent()) {
                        SensorsSnapshotAvro sensorsSnapshotAvro = sensorsSnapshotAvroOpt.get();
                        ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(
                                topics.get(KafkaTopic.SENSORS_SNAPSHOTS),
                                null,
                                sensorsSnapshotAvro.getTimestamp().getEpochSecond(),
                                null,
                                sensorsSnapshotAvro);
                        producer.send(producerRecord);
                        manageOffsets(record, count, consumer);
                        log.info("Snapshot with hub ID {} sent to topic {}", sensorsSnapshotAvro.getHubId(), producerRecord.topic());
                        count++;
                    }
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Error during processing of events from sensors", e);
        } finally {

            try {
                // перед тем, как закрыть продюсер и консьюмер, нужно убедиться,
                // что все сообщения, лежащие в буфере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы

                // вызываем метод продюсера для сброса данных в буфере
                producer.flush();
                // вызываем метод  консьюмера для фиксации смещений
                consumer.commitSync();

            } finally {
                log.info("Closing the consumer");
                consumer.close();
                log.info("Closing the producer");
                producer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<String, SensorEventAvro> record, int count, KafkaConsumer<String, SensorEventAvro> consumer) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % COUNT_FIX_OFFSETS == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Error during offset fixing: {}", offsets, exception);
                }
            });
        }
    }
}