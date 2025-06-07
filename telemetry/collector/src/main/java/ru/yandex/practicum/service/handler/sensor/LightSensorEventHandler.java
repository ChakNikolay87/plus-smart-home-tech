package ru.yandex.practicum.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.EventKafkaProducer;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;

@Component
public class LightSensorEventHandler extends BaseSensorEventHandler<LightSensorAvro> {
    protected LightSensorEventHandler(EventKafkaProducer producer) {
        super(producer);
    }

    @Override
    protected LightSensorAvro mapToAvro(SensorEventProto event) {
        LightSensorProto lightSensorEvent = event.getLightSensorEvent();

        return LightSensorAvro.newBuilder()
                .setLinkQuality(lightSensorEvent.getLinkQuality())
                .setLuminosity(lightSensorEvent.getLuminosity())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getEventType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_EVENT;
    }
}
