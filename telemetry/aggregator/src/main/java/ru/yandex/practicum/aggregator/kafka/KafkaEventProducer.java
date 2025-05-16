package ru.yandex.practicum.aggregator.kafka;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.serialization.AvroSerializer;

import java.util.Properties;
import java.util.concurrent.Future;

@Component
public class KafkaEventProducer {

    private final KafkaProducer<String, SpecificRecordBase> producer;

    public KafkaEventProducer() {
        this.producer = new KafkaProducer<>(producerProperties());
    }

    private Properties producerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
        return props;
    }

    public Future<RecordMetadata> send(String topic, String key, SpecificRecordBase value) {
        return producer.send(new ProducerRecord<>(topic, key, value));
    }

    public void flush() {
        producer.flush();
    }

    public void close() {
        producer.close();
    }

    @PreDestroy
    public void shutdown() {
        producer.flush();
        producer.close();
    }
}

