package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.service.handler.HubProtoHandler;
import ru.yandex.practicum.telemetry.collector.service.handler.SensorProtoHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@GrpcService
@Slf4j
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final Map<SensorEventProto.PayloadCase, SensorProtoHandler> sensorProtoHandlers;
    private final Map<HubEventProto.PayloadCase, HubProtoHandler> hubProtoHandlers;


    public EventController(Set<SensorProtoHandler> sensorEventHandlers, Set<HubProtoHandler> hubEventHandlers) {
        this.sensorProtoHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorProtoHandler::getMessageType, Function.identity()));
        this.hubProtoHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubProtoHandler::getMessageType, Function.identity()));

    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("\nEventController.collectSensorEvent: (toString) accepted {}", request.toString());
        if (request.getPayloadCase().equals(SensorEventProto.PayloadCase.CLIMATE_SENSOR_EVENT)) {
            log.info("\nUnknown fields: {}", request.getClimateSensorEvent().getUnknownFields());
        }
        if (!sensorProtoHandlers.containsKey(request.getPayloadCase())) {
            throw new IllegalArgumentException("Handler for request" + request + " not found.");
        }
        try {
            sensorProtoHandlers.get(request.getPayloadCase()).handle(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("\nEventController.collectHubEvent: accepted {}", request);
        if (!hubProtoHandlers.containsKey(request.getPayloadCase())) {
            throw new IllegalArgumentException("Handler for request" + request + " not found.");
        }
        try {
            hubProtoHandlers.get(request.getPayloadCase()).handle(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

}
