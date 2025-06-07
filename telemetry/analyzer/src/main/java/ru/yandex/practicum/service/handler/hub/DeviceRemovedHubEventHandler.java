package ru.yandex.practicum.service.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRemovedHubEventHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        DeviceRemovedEventAvro deviceRemovedEventAvro = (DeviceRemovedEventAvro) event.getPayload();
        log.info("Received device removed event. Device ID {} in hub ID {}", deviceRemovedEventAvro.getId(), event.getHubId());
        if (!sensorRepository.existsByIdInAndHubId(List.of(deviceRemovedEventAvro.getId()), event.getHubId())) {
            log.warn("Device with ID {} in hub ID {} not found", deviceRemovedEventAvro.getId(), event.getHubId());
        }

        sensorRepository.deleteById(deviceRemovedEventAvro.getId());
        log.info("Device with ID {} removed from hub ID {}", deviceRemovedEventAvro.getId(), event.getHubId());
    }

    @Override
    public String getEventType() {
        return DeviceRemovedEventAvro.class.getName();
    }
}
