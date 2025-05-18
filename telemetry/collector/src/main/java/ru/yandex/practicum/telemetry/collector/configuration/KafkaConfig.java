package ru.yandex.practicum.telemetry.collector.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties("collector.kafka")
public class KafkaConfig {
    private ProducerConfig producer;
    private final String configName = "collector-producer";

    @PostConstruct
    public void checkInit() {
        if (this.producer == null) {
            throw new IllegalStateException("KafkaConfig: producer не инициализирован!");
        }
    }
}
