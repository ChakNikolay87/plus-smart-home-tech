package ru.yandex.practicum.telemetry.aggregator.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@RequiredArgsConstructor
@ConfigurationProperties("aggregator.kafka.consumer")
public class SensorsConsumerConfig {
    private final SensorsConsumerProperties consumerConfig;
    //используется в качестве ключа в Map консюмеров и продюсеров в KafkaClient
    public static final String CONFIG_NAME = "aggregator-consumer";


}
