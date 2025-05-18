package ru.yandex.practicum.telemetry.aggregator.configuration;

import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.TopicType;

import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("aggregator.kafka.producer")
public class SnapshotsProducerProperties {
    private final Map<String, String> properties;
    private final Map<TopicType, String> topics;

    @ConstructorBinding
    public SnapshotsProducerProperties(Map<String, String> properties, Map<String, String> topics) {
        this.properties = properties;
        this.topics = new EnumMap<>(TopicType.class);
        for (Map.Entry<String, String> entry : topics.entrySet()) {
            this.topics.put(TopicType.fromString(entry.getKey()), entry.getValue());
        }
    }

    @PostConstruct
    public void logConfig() {
        System.out.println("Snapshots Producer Properties: " + properties);
        System.out.println("Snapshots Producer Topics: " + topics);
    }
}
