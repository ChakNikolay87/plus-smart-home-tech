package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@ConfigurationPropertiesScan
@EnableCaching
@SpringBootApplication
public class AnalyzerApp {
    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApp.class, args);
    }
}