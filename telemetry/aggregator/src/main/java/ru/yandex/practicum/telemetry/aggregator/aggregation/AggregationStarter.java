package ru.yandex.practicum.telemetry.aggregator.aggregation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.client.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.configuration.KafkaConsumerConfig;
import ru.yandex.practicum.telemetry.aggregator.configuration.KafkaProducerConfig;

import java.time.Duration;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaClient client;
    private final KafkaProducerConfig kafkaProducerConfig;
    private final KafkaConsumerConfig kafkaConsumerConfig;
    protected KafkaConsumer<String, SpecificRecordBase> consumer;
    protected KafkaProducer<String, SpecificRecordBase> producer;
    private final AggregationState aggregationState;

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */

    public void start() {
        consumer = client.getKafkaConsumer(kafkaConsumerConfig.getConfigName(),
                        kafkaConsumerConfig.getConsumerConfig().getProperties());
        consumer.subscribe(kafkaConsumerConfig.getConsumerConfig().getTopics().values().stream().toList());
        producer = client.getKafkaProducer(kafkaProducerConfig.getConfigName(),
                kafkaProducerConfig.getProducerConfig().getProperties());

        try {
            // Цикл обработки событий
            while (true) {
                try {
                    ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(5000));
                    if (!records.isEmpty()) {
                        for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                            // Здесь происходит обработка полученных данных
                                    SensorEventAvro sensorEventAvro = (SensorEventAvro) record.value();
                                    SensorsSnapshotAvro thisSnapshot = aggregationState.sensorEventHandle(sensorEventAvro);
                                    if (thisSnapshot != null)
                                        producer.send(new ProducerRecord<>("telemetry.snapshots.v1", null, thisSnapshot));
                        }
                        consumer.commitSync();
                    }
                } catch (WakeupException e) {
                    // Нормальный выход из цикла при остановке
                    log.info("Консьюмер был остановлен.");
                    break;
                } catch (Exception e) {
                    log.error("Ошибка при обработке данных от датчиков", e);
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмера и продюсера в блоке finally
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