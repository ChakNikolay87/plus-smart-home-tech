package ru.yandex.practicum.telemetry.aggregator.configuration;

import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.TopicType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("aggregator.kafka.producer")
public class AggregatorProducerConfig {
    private Map<String, String> properties = new HashMap<>();
    private Map<String, String> topics = new HashMap<>();

    public EnumMap<TopicType, String> getParsedTopics() {
        EnumMap<TopicType, String> result = new EnumMap<>(TopicType.class);
        topics.forEach((k, v) -> result.put(TopicType.fromString(k), v));
        return result;
    }

    @PostConstruct
    public void logConfig() {
        System.out.println("Kafka Producer Properties: " + properties);
        System.out.println("Kafka Producer Topics: " + topics);
    }
}
