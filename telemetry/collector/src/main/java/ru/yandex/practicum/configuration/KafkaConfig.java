package ru.yandex.practicum.configuration;

import lombok.*;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.*;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties("collector.kafka")
public class KafkaConfig {
    public ProducerConfig producer;

    public enum TopicType {
        SENSORS_EVENTS, HUBS_EVENTS;

        public static TopicType from(String type) {
            switch (type) {
                case "sensor-events" -> {
                    return TopicType.SENSORS_EVENTS;
                }
                case "hubs-events" -> {
                    return TopicType.HUBS_EVENTS;
                }
                default -> throw new RuntimeException("Topic type not found");

            }
        }
    }

    @Getter
    public static class ProducerConfig {
        public final Properties properties;
        private final EnumMap<TopicType, String> topics = new EnumMap<>(TopicType.class);


        public ProducerConfig(Properties properties, Map<String, String> topics) {
            this.properties = properties;
            for (Map.Entry<String, String> entry : topics.entrySet()) {
                this.topics.put(TopicType.from(entry.getKey()), entry.getValue());
            }
        }
    }

    @Bean
    public KafkaProducer<String, SpecificRecordBase> kafkaProducer() {
        return new KafkaProducer<>(producer.properties);
    }

    @Bean
    public EnumMap<TopicType, String> topics() {
        return producer.topics;
    }

    @Component
    @RequiredArgsConstructor
    public static class KafkaEventProducer {
        private final KafkaProducer<String, SpecificRecordBase> kafkaProducer;
        private final EnumMap<TopicType, String> topics;

        public <T extends SpecificRecordBase> void send(String topic, String key, T event) {
            ProducerRecord<String, SpecificRecordBase> record =
                    new ProducerRecord<>(topic, key, event);
            kafkaProducer.send(record);
        }
    }


}



