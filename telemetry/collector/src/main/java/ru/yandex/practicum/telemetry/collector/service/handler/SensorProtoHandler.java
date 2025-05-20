package ru.yandex.practicum.telemetry.collector.service.handler;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorProtoHandler {

    SensorEventProto.PayloadCase getMessageType();

    void handle(SensorEventProto request);
}
