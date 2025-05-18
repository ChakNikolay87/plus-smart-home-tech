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
@ConfigurationProperties("aggregator.kafka.consumer")
public class KafkaConsumerConfig {
    private AggregatorConsumerConfig consumerConfig;
    private final String configName = "aggregator-consumer";

    public KafkaConsumerConfig(AggregatorConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

}
