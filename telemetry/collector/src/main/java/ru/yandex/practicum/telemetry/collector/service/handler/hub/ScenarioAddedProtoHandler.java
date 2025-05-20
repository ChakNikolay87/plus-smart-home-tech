package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.service.handler.mapper.ProtoActionMapper;
import ru.yandex.practicum.telemetry.collector.service.handler.mapper.ProtoConditionMapper;

@Service
@Slf4j
public class ScenarioAddedProtoHandler extends BaseHubProtoHandler<ScenarioAddedEventAvro> {

    public ScenarioAddedProtoHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto sceEvent = event.getScenarioAdded();

        ScenarioAddedEventAvro.Builder builder = ScenarioAddedEventAvro.newBuilder();
        builder.setName(sceEvent.getName());
        builder.setActions(ProtoActionMapper.actionProtoListToActionList(sceEvent.getActionList()));
        builder.setConditions(ProtoConditionMapper.condProtoListToCondList(sceEvent.getConditionList()));
        return builder.build();
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }
}
