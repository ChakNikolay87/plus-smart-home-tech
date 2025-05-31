package ru.yandex.practicum.telemetry.collector.service.clients;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.utilities.RandomUtil;

import java.time.Instant;

@Component
@Slf4j
public abstract class EventDataProducer {

    // возможно, я неверно понимал задание, но сделал класс к уроку "Отправляем и принимаем сообщения"
    // предполагалось, что он будет использоваться вместе с рандомайзером, который помещен в модуль common
    @GrpcClient("collector")
    private CollectorControllerGrpc.CollectorControllerBlockingStub collectorStub;

    private void sendEvent(SensorEventProto event) {

        log.info("Получил данные: {}", event.getAllFields());
        SensorEventProto thisEvent = getChangedEvent(event);

        log.trace("Отправляю данные: {}", thisEvent.getAllFields());
        GeneratedMessageV3 response = collectorStub.collectSensorEvent(thisEvent);
        log.info("Получил ответ от коллектора: {}", response);
    }

    private SensorEventProto getChangedEvent(SensorEventProto event) {
        Instant ts = Instant.now();
        SensorEventProto.Builder newEventBuilder = SensorEventProto.newBuilder()
                .setId(event.getId())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(ts.getEpochSecond())
                        .setNanos(ts.getNano())
                );

        SensorEventProto.PayloadCase thisCase = event.getPayloadCase();
        switch (thisCase) {
            case MOTION_SENSOR_EVENT -> newEventBuilder
                    .setMotionSensorEvent(createMotionSensorEvent(event.getMotionSensorEvent()));
            case TEMPERATURE_SENSOR_EVENT -> newEventBuilder
                    .setTemperatureSensorEvent(createTemperatureSensorEvent(event.getTemperatureSensorEvent()));
            case LIGHT_SENSOR_EVENT -> newEventBuilder
                    .setLightSensorEvent(createLightSensorEvent(event.getLightSensorEvent()));
            case CLIMATE_SENSOR_EVENT -> newEventBuilder
                    .setClimateSensorEvent(createClimateSensorEvent(event.getClimateSensorEvent()));
            case SWITCH_SENSOR_EVENT -> newEventBuilder
                    .setSwitchSensorEvent(createSwitchSensorEvent(event.getSwitchSensorEvent()));
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Payload not set: " + event);
        }
        return newEventBuilder.build();
    }

    private TemperatureSensorProto createTemperatureSensorEvent(TemperatureSensorProto event) {
        int temperatureCelsius = RandomUtil.getRandomValue("temperature", event.getTemperatureC());
        int temperatureFahrenheit = (int) (temperatureCelsius * 1.8 + 32);

        return TemperatureSensorProto.newBuilder()
                .setTemperatureC(temperatureCelsius)
                .setTemperatureF(temperatureFahrenheit)
                .build();
    }

    private MotionSensorProto createMotionSensorEvent(MotionSensorProto event) {
        int thisLinkQuality = RandomUtil.getRandomValue("linkQuality", event.getLinkQuality());
        int thisVoltage = RandomUtil.getRandomValue("voltage", event.getVoltage());

        return MotionSensorProto.newBuilder()
                .setLinkQuality(thisLinkQuality)
                .setMotion(event.getMotion())
                .setVoltage(thisVoltage)
                .build();
    }

    private LightSensorProto createLightSensorEvent(LightSensorProto event) {
        int thisLinkQuality = RandomUtil.getRandomValue("linkQuality", event.getLinkQuality());
        int thisLuminosity = RandomUtil.getRandomValue("luminosity", event.getLuminosity());

        return LightSensorProto.newBuilder()
                .setLinkQuality(thisLinkQuality)
                .setLuminosity(thisLuminosity)
                .build();
    }

    private ClimateSensorProto createClimateSensorEvent(ClimateSensorProto event) {
        int temperatureCelsius = RandomUtil.getRandomValue("temperature", event.getTemperatureC());
        int thisHumidity = RandomUtil.getRandomValue("humidity", event.getHumidity());
        int thisCo2Level = RandomUtil.getRandomValue("co2Level", event.getHumidity());

        return ClimateSensorProto.newBuilder()
                .setTemperatureC(temperatureCelsius)
                .setHumidity(thisHumidity)
                .setCo2Level(thisCo2Level)
                .build();
    }

    private SwitchSensorProto createSwitchSensorEvent(SwitchSensorProto event) {
        return SwitchSensorProto.newBuilder()
                .setState(event.getState())
                .build();
    }

}
