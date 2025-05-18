package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.client.KafkaClient;
import ru.yandex.practicum.configuration.HubsConsumerConfig;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.HubHandler;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final KafkaClient client;
    private final HubsConsumerConfig consumerConfig;
    private final HubHandler handler;
    protected KafkaConsumer<String, SpecificRecordBase> consumer;


    @Override
    public void run() {
        consumer = client.getKafkaConsumer(consumerConfig.getHubsConsumerProperties().getProperties());
        consumer.subscribe(consumerConfig.getHubsConsumerProperties().getTopics().values().stream().toList());
        log.info("HubEventProcessor: Subscribed to topic: {}", consumer.subscription());
        try {
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(5000));
                if (!records.isEmpty()) {
                    log.info("\nHubEventProcessor: accepted " + records);
                    for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                        HubEventAvro event = (HubEventAvro) record.value();
                        handler.habEventHandle(event);
                    }
                }
            }
        } catch (WakeupException e) {
            log.info("Consumer wakeup called, shutting down");
        } catch (Exception e) {
            log.error("Unexpected error in HubEventProcessor", e);
        } finally {
            consumer.close();
        }
    }

    public void start() {
    }

}