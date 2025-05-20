package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotProcessorManager {

    private final SnapshotProcessor processor;
    private Thread processorThread;

    public void start() {
        if (processorThread == null || !processorThread.isAlive()) {
            processorThread = new Thread(processor, "SnapshotProcessorThread");
            processorThread.start();
            log.info("SnapshotProcessor thread started");
        } else {
            log.warn("SnapshotProcessor thread is already running");
        }
    }

    public void stop() {
        if (processorThread != null && processorThread.isAlive()) {
            processor.shutdown();
            try {
                processorThread.join();
                log.info("SnapshotProcessor thread stopped");
            } catch (InterruptedException e) {
                log.error("Interrupted while stopping SnapshotProcessor thread", e);
                Thread.currentThread().interrupt();
            }
        } else {
            log.warn("SnapshotProcessor thread is not running");
        }
    }
}
