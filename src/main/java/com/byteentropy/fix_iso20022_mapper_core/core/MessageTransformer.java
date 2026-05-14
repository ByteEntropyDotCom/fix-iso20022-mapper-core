package com.byteentropy.fix_iso20022_mapper_core.core;

import com.byteentropy.fix_iso20022_mapper_core.config.AppConfig;
import com.byteentropy.fix_iso20022_mapper_core.mapping.TransformationStrategy;
import com.byteentropy.fix_iso20022_mapper_core.infra.KafkaEgressClient;
import com.byteentropy.fix_iso20022_mapper_core.infra.TelemetryService;
import com.byteentropy.fix_iso20022_mapper_core.model.RawFixMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageTransformer {
    private static final Logger log = LoggerFactory.getLogger(MessageTransformer.class);
    private final List<TransformationStrategy> strategies;
    private final KafkaEgressClient kafkaClient;
    private final TelemetryService telemetry;
    private final AppConfig appConfig;

    public MessageTransformer(List<TransformationStrategy> strategies, 
                              KafkaEgressClient kafkaClient, 
                              TelemetryService telemetry,
                              AppConfig appConfig) {
        this.strategies = strategies;
        this.kafkaClient = kafkaClient;
        this.telemetry = telemetry;
        this.appConfig = appConfig;
    }

    public void transformAndSend(RawFixMessage message) {
        try {
            // Pick the strategy that matches the FIX type AND the target Market
            TransformationStrategy strategy = strategies.stream()
                    .filter(s -> s.supports(message.payload()))
                    .filter(s -> s.getMarket().equalsIgnoreCase(appConfig.market()))
                    .findFirst()
                    .orElse(null);

            if (strategy == null) {
                log.warn("No strategy found for Market: {} and Message Type", appConfig.market());
                return;
            }

            String isoXml = strategy.toIso(message.payload());
            kafkaClient.send(isoXml);

        } catch (Exception e) {
            log.error("Pipeline Error: {}", e.getMessage());
        } finally {
            telemetry.recordLatency(System.nanoTime() - message.ingressTimestamp());
        }
    }
}