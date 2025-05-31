package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.SnapshotConsumerConfig;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.client.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.HubRouterGrpcProducer;
import ru.yandex.practicum.service.SnapshotHandler;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class SnapshotProcessor implements Runnable {

    private final KafkaClient client;
    private final SnapshotConsumerConfig consumerConfig;
    private final SnapshotHandler handler;
    private final HubRouterGrpcProducer hubRouterGrpcProducer;

    private final Consumer<String, SpecificRecordBase> consumer;

    private volatile boolean running = true;

    public SnapshotProcessor(KafkaClient client,
                             SnapshotConsumerConfig consumerConfig,
                             SnapshotHandler handler,
                             HubRouterGrpcProducer hubRouterGrpcProducer) {
        this.client = client;
        this.consumerConfig = consumerConfig;
        this.handler = handler;
        this.hubRouterGrpcProducer = hubRouterGrpcProducer;

        this.consumer = client.getConsumer(consumerConfig.getSnapshotConsumerProperties().getProperties());
        this.consumer.subscribe(consumerConfig.getSnapshotConsumerProperties().getTopics().values().stream().toList());

        log.info("SnapshotProcessor: Subscribed to topic: {}", consumer.subscription());
    }

    @Override
    public void run() {
        try {
            while (running) {
                var records = consumer.poll(Duration.ofMillis(1000));
                if (!records.isEmpty()) {
                    log.info("SnapshotProcessor: records {}", records);
                    for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                        SensorsSnapshotAvro snapshot = (SensorsSnapshotAvro) record.value();
                        List<DeviceActionRequest> messageList = handler.process(snapshot);
                        if (!messageList.isEmpty()) {
                            log.info("Send {}", messageList);
                            sendToGrpc(messageList);
                        }
                    }
                }
            }
        } catch (WakeupException e) {
            if (running) {
                log.error("WakeupException in SnapshotProcessor", e);
            }
        } catch (Exception e) {
            log.error("Unexpected error in SnapshotProcessor", e);
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        running = false;
        consumer.wakeup();
    }

    private void sendToGrpc(List<DeviceActionRequest> requests) {
        for (DeviceActionRequest request : requests) {
            hubRouterGrpcProducer.sendRequest(request);
        }
    }
}
