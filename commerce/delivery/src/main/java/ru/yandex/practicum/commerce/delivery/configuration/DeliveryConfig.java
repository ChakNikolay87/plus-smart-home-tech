package ru.yandex.practicum.commerce.delivery.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
