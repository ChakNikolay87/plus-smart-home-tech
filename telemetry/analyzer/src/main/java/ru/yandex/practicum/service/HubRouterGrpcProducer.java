package ru.yandex.practicum.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Service
public class HubRouterGrpcProducer {
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public HubRouterGrpcProducer(@GrpcClient("hub-router")
                                 HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }

    public void sendRequest(DeviceActionRequest request) {
        hubRouterClient.handleDeviceAction(request);
    }
}
