package com.byteentropy.fix_iso20022_mapper_core.core;

import com.byteentropy.fix_iso20022_mapper_core.model.RawFixMessage;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class PipelineDispatcher {
    private static final Logger log = LoggerFactory.getLogger(PipelineDispatcher.class);
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final MessageTransformer transformer;

    public PipelineDispatcher(MessageTransformer transformer) {
        this.transformer = transformer;
    }

    public void dispatch(RawFixMessage message) {
        executor.submit(() -> transformer.transformAndSend(message));
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Pipeline Dispatcher...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}