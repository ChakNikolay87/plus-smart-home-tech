package ru.yandex.practicum.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.EventKafkaProducer;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;

@Component
public class ScenarioAddedHubEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {
    protected ScenarioAddedHubEventHandler(EventKafkaProducer producer) {
        super(producer);
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto scenarioAddedEvent = event.getScenarioAdded();

        List<ScenarioConditionAvro> conditions = scenarioAddedEvent.getConditionList().stream().map(this::mapToConditionAvro).toList();
        List<DeviceActionAvro> actions = scenarioAddedEvent.getActionList().stream().map(this::mapToActionAvro).toList();

        return ScenarioAddedEventAvro.newBuilder()
                .setConditions(conditions)
                .setActions(actions)
                .setName(scenarioAddedEvent.getName())
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getEventType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    private ScenarioConditionAvro mapToConditionAvro(ScenarioConditionProto scenarioCondition) {
        return ScenarioConditionAvro.newBuilder()
                .setType(ConditionTypeAvro.valueOf(scenarioCondition.getType().name()))
                .setSensorId(scenarioCondition.getSensorId())
                .setValue(scenarioCondition.hasBoolValue() ? scenarioCondition.getBoolValue() : scenarioCondition.getIntValue())
                .setOperation(ConditionOperationAvro.valueOf(scenarioCondition.getOperation().name()))
                .build();
    }

    private DeviceActionAvro mapToActionAvro(DeviceActionProto deviceAction) {
        return DeviceActionAvro.newBuilder()
                .setType(ActionTypeAvro.valueOf(deviceAction.getType().name()))
                .setSensorId(deviceAction.getSensorId())
                .setValue(deviceAction.getValue())
                .build();
    }
}
