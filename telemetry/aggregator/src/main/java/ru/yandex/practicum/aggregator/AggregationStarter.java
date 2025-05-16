package ru.yandex.practicum.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.kafka.KafkaEventConsumer;
import ru.yandex.practicum.aggregator.kafka.KafkaEventProducer;
import ru.yandex.practicum.aggregator.service.SensorSnapshotService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaEventConsumer consumer;
    private final KafkaEventProducer producer;
    private final SensorSnapshotService snapshotService;

    public void start() {
        log.info("Инициализация подписки на топик telemetry.sensors.v1");
        consumer.subscribe(List.of("telemetry.sensors.v1"));

        try {
            while (true) {
                log.debug("Ожидание новых сообщений из Kafka...");
                var records = consumer.poll(Duration.ofMillis(100));
                log.debug("Получено {} сообщений", records.count());
                for (var record : records) {
                    var event = record.value();
                    log.debug("Обработка события от датчика: {}", event);
                    try {
                        snapshotService.updateSnapshot(event).ifPresent(snapshot -> {
                            try {
                                producer.send(
                                        "telemetry.snapshots.v1",
                                        snapshot.getHubId(),
                                        snapshot
                                );
                                log.info("Отправлен снепшот хаба: {}", snapshot.getHubId());
                            } catch (Exception e) {
                                log.error("Ошибка при отправке снепшота в Kafka: hubId={}", snapshot.getHubId(), e);
                            }
                        });
                    } catch (Exception e) {
                        log.error("Ошибка при обновлении снепшота по событию: {}", event, e);
                    }
                }
                try {
                    consumer.commit();
                    log.debug("Коммит смещений выполнен успешно");
                } catch (Exception e) {
                    log.error("Ошибка при коммите смещений", e);
                }
            }

        } catch (WakeupException e) {
            log.info("Получен сигнал завершения (WakeupException)");
        } catch (Exception e) {
            log.error("Критическая ошибка во время обработки событий", e);
        } finally {
            try {
                producer.flush();
                log.info("Продюсер сбросил данные из буфера");

                consumer.commit();
                log.info("Финальный коммит смещений выполнен");

            } catch (Exception e) {
                log.error("Ошибка при финальной очистке ресурсов", e);
            } finally {
                try {
                    log.info("Закрытие продюсера...");
                    producer.close();
                    log.info("Продюсер успешно закрыт");
                } catch (Exception e) {
                    log.error("Ошибка при закрытии продюсера", e);
                }
                try {
                    log.info("Закрытие консьюмера...");
                    consumer.close();
                    log.info("Консьюмер успешно закрыт");
                } catch (Exception e) {
                    log.error("Ошибка при закрытии консьюмера", e);
                }
            }
        }
    }

}
