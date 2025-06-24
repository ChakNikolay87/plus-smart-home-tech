package ru.yandex.practicum.commerce.order.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
