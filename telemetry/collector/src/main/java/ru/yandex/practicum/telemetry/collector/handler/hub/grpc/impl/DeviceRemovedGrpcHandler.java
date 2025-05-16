package ru.yandex.practicum.telemetry.collector.handler.hub.grpc.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.telemetry.collector.handler.hub.grpc.GrpcHubEventHandler;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.mapper.grpc.GrpcHubEventMapper;

@Component
@RequiredArgsConstructor
public class DeviceRemovedGrpcHandler implements GrpcHubEventHandler {
    private final KafkaEventProducer producer;
    private static final String TOPIC = "telemetry.hubs.v1";

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        var avro = GrpcHubEventMapper.toAvro(event);
        producer.send(TOPIC, event.getHubId(), avro);
    }
}
