package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.model.device.types.DeviceAction;
import ru.yandex.practicum.model.device.types.ScenarioCondition;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.mappers.ActionMapper;
import ru.yandex.practicum.mappers.ConditionsMapper;
import ru.yandex.practicum.telemetry.collector.service.handler.mapper.ProtoActionMapper;
import ru.yandex.practicum.telemetry.collector.service.handler.mapper.ProtoConditionMapper;

import java.util.List;

@Service
@Slf4j
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {

    public ScenarioAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto sceEvent = event.getScenarioAdded();

        List<ScenarioCondition> condList =
                ProtoConditionMapper.condProtoListToCondList(sceEvent.getConditionList());
        List<DeviceAction> actList =
                ProtoActionMapper.actionProtoListToActionList(sceEvent.getActionList());

        ScenarioAddedEventAvro.Builder builder = ScenarioAddedEventAvro.newBuilder();
        builder.setName(sceEvent.getName());
        builder.setActions(ActionMapper.devAtcListToAvro(actList));
        builder.setConditions(ConditionsMapper.condListToAvro(condList));
        return builder.build();
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }
}
