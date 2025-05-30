package ru.yandex.practicum.telemetry.aggregator.aggregation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.client.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.configuration.SensorsConsumerConfig;
import ru.yandex.practicum.telemetry.aggregator.configuration.SnapshotsProducerConfig;

import java.time.Duration;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaClient client;
    private final SnapshotsProducerConfig snapshotsProducerConfig;
    private final SensorsConsumerConfig sensorsConsumerConfig;
    protected Consumer<String, SpecificRecordBase> consumer;
    protected Producer<String, SpecificRecordBase> producer;
    private final AggregationState aggregationState;

    public void start() {
        consumer = client.getConsumer(sensorsConsumerConfig.getConsumerConfig().getProperties());
        consumer.subscribe(sensorsConsumerConfig.getConsumerConfig().getTopics().values().stream().toList());
        producer = client.getProducer(snapshotsProducerConfig.getProducerConfig().getProperties());

        try {
            while (true) {
                try {
                    ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(5000));
                    if (!records.isEmpty()) {
                        log.trace("\nAggregationStarter: accepted {}", records);
                        for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                            SensorEventAvro sensorEventAvro = (SensorEventAvro) record.value();
                            SensorsSnapshotAvro thisSnapshot = aggregationState.sensorEventHandle(sensorEventAvro);
                            if (thisSnapshot != null) {
                                log.trace("\nAggregationStarter: new snapshot for send {}", thisSnapshot);
                                producer.send(new ProducerRecord<>("telemetry.snapshots.v1", null, thisSnapshot));
                            }
                        }
                        consumer.commitSync();
                    }
                } catch (WakeupException e) {
                    log.info("Консьюмер был остановлен.");
                    break;
                } catch (Exception e) {
                    log.error("Ошибка при обработке данных от датчиков", e);
                }
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}
