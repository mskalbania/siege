package com.reactive.siege.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SiegeStateContainer {

    private static final Logger LOG = LoggerFactory.getLogger(SiegeStateContainer.class);

    private LocalDateTime startTime;
    private AtomicInteger tpsCounter;
    private AtomicLong volumeCounterBytes;
    private boolean isRunning;
    private ScheduledExecutorService tpsLoggerService;

    public SiegeStateContainer() {
        setInitial();
    }

    public void started() {
        setInitial();
        isRunning = true;
        startTime = LocalDateTime.now();
        tpsLoggerService.scheduleAtFixedRate(() -> {
            long timeRunning = Duration.between(startTime, LocalDateTime.now()).getSeconds();
            long allSent = tpsCounter.get();
            LOG.info("Running @ ~{} transactions/s | Total of {} kB retrieved", allSent / timeRunning, volumeCounterBytes.get() / 1024);
        }, 10, 5, TimeUnit.SECONDS);
    }

    public void stopped() {
        isRunning = false;
        tpsLoggerService.shutdown();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void incrementTransactionCount() {
        tpsCounter.incrementAndGet();
    }

    public void incrementVolume(String payload) {
        if (payload != null) {
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            volumeCounterBytes.getAndAdd(bytes.length);
        }
    }

    public SiegeResult toSiegeResult() {
        Duration timeRunning = Duration.between(startTime, LocalDateTime.now());
        int allSent = tpsCounter.get();
        long averageTps = allSent / timeRunning.getSeconds();
        return new SiegeResult(timeRunning, averageTps, allSent, volumeCounterBytes.get() / 1024);
    }

    private void setInitial() {
        tpsCounter = new AtomicInteger(0);
        volumeCounterBytes = new AtomicLong(0);
        isRunning = false;
        tpsLoggerService = Executors.newSingleThreadScheduledExecutor();
    }
}
