package ru.yandex.practicum.kafka.client;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class KafkaClientImpl implements KafkaClient {

    private final Map<String, Producer<String, SpecificRecordBase>> producerMap = new ConcurrentHashMap<>();
    private final Map<String, Consumer<String, SpecificRecordBase>> consumerMap = new ConcurrentHashMap<>();
    private final Map<String, KafkaProducer<String, SpecificRecordBase>> kafkaProducerMap = new ConcurrentHashMap<>();
    private final Map<String, KafkaConsumer<String, SpecificRecordBase>> kafkaConsumerMap = new ConcurrentHashMap<>();

    @Override
    public Producer<String, SpecificRecordBase> getProducer(String configName, Map<String, String> properties) {
        return producerMap.computeIfAbsent(configName, key -> createProducer(properties));
    }

    @Override
    public Consumer<String, SpecificRecordBase> getConsumer(String configName, Map<String, String> properties) {
        return consumerMap.computeIfAbsent(configName, key -> createConsumer(properties));
    }

    @Override
    public KafkaProducer<String, SpecificRecordBase> getKafkaProducer(String configName, Map<String, String> properties) {
        return kafkaProducerMap.computeIfAbsent(configName, key -> createKafkaProducer(properties));
    }

    @Override
    public KafkaConsumer<String, SpecificRecordBase> getKafkaConsumer(String configName, Map<String, String> properties) {
        return kafkaConsumerMap.computeIfAbsent(configName, key -> createKafkaConsumer(properties));
    }

    private Producer<String, SpecificRecordBase> createProducer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        return new KafkaProducer<>(props);
    }

    private Consumer<String, SpecificRecordBase> createConsumer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        return new KafkaConsumer<>(props);
    }

    private KafkaProducer<String, SpecificRecordBase> createKafkaProducer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        return new KafkaProducer<>(props);
    }

    private KafkaConsumer<String, SpecificRecordBase> createKafkaConsumer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        return new KafkaConsumer<>(props);
    }

    @Override
    @PreDestroy
    public void stop() {
        producerMap.values().forEach(Producer::close);
        consumerMap.values().forEach(Consumer::close);
        kafkaProducerMap.values().forEach(Producer::close);
        kafkaConsumerMap.values().forEach(Consumer::close);

    }
}
