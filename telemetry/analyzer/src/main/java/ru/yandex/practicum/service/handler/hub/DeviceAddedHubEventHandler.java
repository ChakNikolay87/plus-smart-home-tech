package ru.yandex.practicum.service.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedHubEventHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        DeviceAddedEventAvro deviceAddedEventAvro = (DeviceAddedEventAvro) event.getPayload();
        log.info("Received device added event. Device ID {} in hub ID {}", deviceAddedEventAvro.getId(), event.getHubId());
        if (sensorRepository.existsByIdInAndHubId(List.of(deviceAddedEventAvro.getId()), event.getHubId())) {
            log.warn("Device with ID {} in hub ID {} already exists", deviceAddedEventAvro.getId(), event.getHubId());
            return;
        }
        Sensor sensor = Sensor.builder()
                .hubId(event.getHubId())
                .id(deviceAddedEventAvro.getId())
                .build();
        sensorRepository.save(sensor);
        log.info("Device with ID {} added to hub ID {}", deviceAddedEventAvro.getId(), event.getHubId());
    }

    @Override
    public String getEventType() {
        return DeviceAddedEventAvro.class.getName();
    }
}
