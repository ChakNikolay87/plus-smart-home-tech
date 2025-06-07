package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaConfig;
import ru.yandex.practicum.configuration.KafkaTopic;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.handler.hub.HubEventHandler;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final EnumMap<KafkaTopic, String> topics;
    private final Map<String, HubEventHandler> hubEventHandlers;

    public HubEventProcessor(KafkaConfig kafkaConfig, List<HubEventHandler> hubEventHandlers) {
        consumer = new KafkaConsumer<>(kafkaConfig.getHubEventConsumerProps());
        topics = kafkaConfig.getTopics();
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getEventType, Function.identity()));
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(topics.get(KafkaTopic.HUBS_EVENTS)));

        // регистрируем хук при завершении JVM
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            // Цикл обработки событий
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (records.isEmpty()) continue;

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    log.info("Received record: topic = {}, partition = {}, offset = {}, value = {}",
                            record.topic(), record.partition(), record.offset(), record.value());
                    // обработка событий от хабов
                    String eventClassName = record.value().getPayload().getClass().getName();
                    if (!hubEventHandlers.containsKey(eventClassName)) {
                        log.warn("No handler found for event class: {}", eventClassName);
                        throw new IllegalArgumentException("Unknown event class: " + eventClassName);
                    }
                    hubEventHandlers.get(eventClassName).handle(record.value());
                }

                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Error during offset fixing. Offset: {}", offsets, exception);
                    }
                });
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер в блоке finally
        } catch (Exception e) {
            log.error("Error during processing of events from sensors", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Closing the consumer");
                consumer.close();
            }
        }
    }
}
