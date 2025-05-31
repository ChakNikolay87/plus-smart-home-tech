
package ru.yandex.practicum.kafka.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class KafkaClient {

    // Кешируем продюсеры и консьюмеры по их свойствам
    private final Map<Map<String, String>, Producer<String, SpecificRecordBase>> producersCache = new ConcurrentHashMap<>();
    private final Map<Map<String, String>, Consumer<String, SpecificRecordBase>> consumersCache = new ConcurrentHashMap<>();

    public Producer<String, SpecificRecordBase> getProducer(Map<String, String> properties) {
        return producersCache.computeIfAbsent(properties, this::createProducer);
    }

    public Consumer<String, SpecificRecordBase> getConsumer(Map<String, String> properties) {
        return consumersCache.computeIfAbsent(properties, this::createConsumer);
    }

    private Producer<String, SpecificRecordBase> createProducer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        log.info("Creating new KafkaProducer with properties: {}", properties);
        return new KafkaProducer<>(props);
    }

    private Consumer<String, SpecificRecordBase> createConsumer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        log.info("Creating new KafkaConsumer with properties: {}", properties);
        return new KafkaConsumer<>(props);
    }

    @PreDestroy
    public void stop() {
        log.info("Closing all Kafka producers");
        producersCache.values().forEach(producer -> {
            try {
                producer.close();
            } catch (Exception e) {
                log.warn("Error closing producer", e);
            }
        });
        log.info("Closing all Kafka consumers");
        consumersCache.values().forEach(consumer -> {
            try {
                consumer.close();
            } catch (Exception e) {
                log.warn("Error closing consumer", e);
            }
        });
        producersCache.clear();
        consumersCache.clear();
    }
}
