package ru.yandex.practicum.telemetry.aggregator.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("aggregator.kafka.producer")
public class KafkaProducerConfig {
    private AggregatorProducerConfig producerConfig;
    private final String configName = "aggregator-producer";

    public KafkaProducerConfig(AggregatorProducerConfig producerConfig) {
        this.producerConfig = producerConfig;
    }

}
