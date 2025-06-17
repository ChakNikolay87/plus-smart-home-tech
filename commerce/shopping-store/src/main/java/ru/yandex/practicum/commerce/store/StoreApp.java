package ru.yandex.practicum.commerce.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class StoreApp {
    public static void main(String[] args) {
        SpringApplication.run(StoreApp.class, args);
    }
}