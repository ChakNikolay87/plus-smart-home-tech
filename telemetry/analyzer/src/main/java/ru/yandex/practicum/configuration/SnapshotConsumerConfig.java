package ru.yandex.practicum.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@RequiredArgsConstructor
@ConfigurationProperties("analyzer.kafka.consumers.hubs")
public class SnapshotConsumerConfig {
    @Autowired
    private SnapshotConsumerProperties snapshotConsumerProperties;

    @PostConstruct
    public void checkInit() {
        if (this.snapshotConsumerProperties == null) {
            throw new IllegalStateException("SnapshotConsumerConfig: consumer не инициализирован!");
        }
    }
}
