package ru.yandex.practicum.telemetry.analyzer.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.telemetry.analyzer.mapper.GrpcDeviceActionMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HubRouterGrpcClient {

    @GrpcClient("hub-router")
    HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;
    final GrpcDeviceActionMapper grpcDeviceActionMapper;

    public void sendDeviceAction(String hubId, String scenarioName, String sensorId, Integer value, String type) {
        try {
            log.info("Отправка команды устройству по gRPC: hubId={}, scenario={}, sensorId={}, value={}, type={}",
                    hubId, scenarioName, sensorId, value, type);
            DeviceActionProto action = grpcDeviceActionMapper.toDeviceAction(sensorId, type, value);
            DeviceActionRequest request = grpcDeviceActionMapper.toDeviceActionRequest(hubId, scenarioName, action);
            log.warn("Условия выполнены, отправляем команду: {}", action);
            hubRouterStub.handleDeviceAction(request);
            log.info("Команда успешно отправлена через gRPC");
        } catch (Exception e) {
            log.error("Ошибка при отправке команды в gRPC-сервис: {}", e.getMessage(), e);
        }
    }
}
