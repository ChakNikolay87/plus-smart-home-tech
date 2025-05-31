package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

@Service
@Slf4j
public class DeviceAddedProtoHandler extends BaseHubProtoHandler<DeviceAddedEventAvro> {
    public DeviceAddedProtoHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEventProto event) {
        DeviceAddedEventProto thisEvent = event.getDeviceAdded();
        DeviceTypeProto thisType = thisEvent.getType();
        DeviceTypeAvro avroType = DeviceTypeAvro.valueOf(thisType.name());

        DeviceAddedEventAvro devAddAvro = DeviceAddedEventAvro.newBuilder()
                .setId(thisEvent.getId())
                .setType(avroType)
                .build();
        log.trace("\nDeviceAddedEventHandler: devAddAvro {}", devAddAvro);
        return devAddAvro;
    }

}
