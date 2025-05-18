package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

@Service
@Slf4j
public class ClimateSensorEventHandler extends BaseSensorEventHandler<ClimateSensorAvro> {

    public ClimateSensorEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_EVENT;
    }

    @Override
    protected ClimateSensorAvro mapToAvro(SensorEventProto event) {

        ClimateSensorProto thisEvent = event.getClimateSensorEvent();
        log.trace("\nClimateSensorEventHandler:\n {}", thisEvent);
        ClimateSensorAvro newAvroEvent = ClimateSensorAvro.newBuilder()
                .setTemperatureC(thisEvent.getTemperatureC())
                .setHumidity(thisEvent.getHumidity())
                .setCo2Level(thisEvent.getCo2Level())
                .build();
        log.trace("\nFormed {}", newAvroEvent);
        return newAvroEvent;
    }

}
