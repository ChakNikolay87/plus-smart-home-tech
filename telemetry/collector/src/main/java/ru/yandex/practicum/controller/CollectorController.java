package ru.yandex.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.service.handler.sensor.SensorEventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class CollectorController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    public CollectorController(List<SensorEventHandler> sensorEventHandlers, List<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream().collect(Collectors.toMap(SensorEventHandler::getEventType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream().collect(Collectors.toMap(HubEventHandler::getEventType, Function.identity()));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (sensorEventHandlers.containsKey(request.getPayloadCase())) {
                log.info("Collecting sensor event: {}", request.getPayloadCase());
                sensorEventHandlers.get(request.getPayloadCase()).handle(request);
            } else {
                log.error("Unknown sensor type: {}", request.getPayloadCase());
                throw new IllegalArgumentException("Unknown event type: " + request.getPayloadCase());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error collecting sensor event, exception: {}", e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (hubEventHandlers.containsKey(request.getPayloadCase())) {
                log.info("Collecting hub event: {}", request.getPayloadCase());
                hubEventHandlers.get(request.getPayloadCase()).handle(request);
            } else {
                log.error("Unknown hub type: {}", request.getPayloadCase());
                throw new IllegalArgumentException("Unknown event type: " + request.getPayloadCase());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error collecting hub event, exception: {}", e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
