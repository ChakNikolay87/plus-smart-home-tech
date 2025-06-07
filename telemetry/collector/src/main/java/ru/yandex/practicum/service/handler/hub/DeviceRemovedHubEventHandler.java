package ru.yandex.practicum.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.EventKafkaProducer;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;

@Component
public class DeviceRemovedHubEventHandler extends BaseHubEventHandler<DeviceRemovedEventAvro> {
    protected DeviceRemovedHubEventHandler(EventKafkaProducer producer) {
        super(producer);
    }

    @Override
    protected DeviceRemovedEventAvro mapToAvro(HubEventProto event) {
        DeviceRemovedEventProto deviceRemovedEvent = event.getDeviceRemoved();

        return DeviceRemovedEventAvro.newBuilder()
                .setId(deviceRemovedEvent.getId())
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getEventType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }
}
