package ru.yandex.practicum.kafka.deserializer;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
public class HubEventAvroDeserializer extends BaseAvroDeserializer<HubEventAvro> {
    public HubEventAvroDeserializer() {
        super(HubEventAvro.getClassSchema());
    }
}
