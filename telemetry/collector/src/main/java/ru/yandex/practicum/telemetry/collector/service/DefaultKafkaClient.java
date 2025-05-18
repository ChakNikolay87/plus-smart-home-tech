package ru.yandex.practicum.telemetry.collector.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.configuration.KafkaConfig;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class DefaultKafkaClient implements KafkaClient {

    private final KafkaConfig kafkaConfig;
    private Producer<String, SpecificRecordBase> producer;

    @PostConstruct
    public void init() {

        if (kafkaConfig == null) {
            throw new IllegalStateException("kafkaConfig не был внедрен Spring'ом!");
        }

        if (kafkaConfig.getProducer() == null) {
            throw new IllegalStateException("KafkaConfig.getProducer() вернул null");
        }

        Properties properties = new Properties();
        properties.putAll(kafkaConfig.getProducer().getProperties());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer.class.getName());

        this.producer = new KafkaProducer<>(properties);
    }

    @Override
    public Producer<String, SpecificRecordBase> getProducer() {
        return producer;
    }

    @Override
    public Consumer<String, SpecificRecordBase> getConsumer() {
        return null;
    }

    @Override
    @PreDestroy
    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }
}