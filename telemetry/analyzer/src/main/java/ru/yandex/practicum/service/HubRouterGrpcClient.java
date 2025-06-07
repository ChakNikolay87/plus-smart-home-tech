package ru.yandex.practicum.service;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Slf4j
@Service
public class HubRouterGrpcClient {
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public HubRouterGrpcClient(@GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }

    public void sendDeviceAction(DeviceActionRequest deviceActionRequest) {
        try {
            hubRouterClient.handleDeviceAction(deviceActionRequest);
            log.info("Hub device action sent to hub router");
        } catch (StatusRuntimeException e) {
            log.error("Error sending device action", e);
        }
    }
}
