package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnalyzerRunner implements CommandLineRunner {
    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;

    public AnalyzerRunner(HubEventProcessor hubEventProcessor, SnapshotProcessor snapshotProcessor) {
        this.hubEventProcessor = hubEventProcessor;
        this.snapshotProcessor = snapshotProcessor;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread hubEventThread = new Thread(hubEventProcessor);
        hubEventThread.setName("HubEventHandlerThread");
        hubEventThread.start();
        log.info("Hub event processor started");

        snapshotProcessor.start();
        log.info("Snapshot processor started");
    }
}
