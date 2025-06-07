package ru.yandex.practicum.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.EventKafkaProducer;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Component
public class DeviceAddedHubEventHandler extends BaseHubEventHandler<DeviceAddedEventAvro> {
    protected DeviceAddedHubEventHandler(EventKafkaProducer producer) {
        super(producer);
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEventProto event) {
        DeviceAddedEventProto deviceAddedEvent = event.getDeviceAdded();

        return DeviceAddedEventAvro.newBuilder()
                .setId(deviceAddedEvent.getId())
                .setType(DeviceTypeAvro.valueOf(deviceAddedEvent.getType().name()))
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getEventType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }
}
