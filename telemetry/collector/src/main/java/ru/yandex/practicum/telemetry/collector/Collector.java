package ru.yandex.practicum.telemetry.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.yandex.practicum.telemetry.collector.configuration.KafkaConfig;
import ru.yandex.practicum.telemetry.collector.configuration.ProducerConfig;

@SpringBootApplication
@EnableConfigurationProperties({KafkaConfig.class, ProducerConfig.class})
public class Collector {
    public static void main(String[] args) {
        SpringApplication.run(Collector.class, args);
    }
}