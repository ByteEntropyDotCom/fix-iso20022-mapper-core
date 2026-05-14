package com.byteentropy.fix_iso20022_mapper_core.infra;

import com.byteentropy.fix_iso20022_mapper_core.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEgressClient {
    private static final Logger log = LoggerFactory.getLogger(KafkaEgressClient.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AppConfig appConfig;

    public KafkaEgressClient(KafkaTemplate<String, String> kafkaTemplate, AppConfig appConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.appConfig = appConfig;
    }

    public void send(String payload) {
        try {
            if (appConfig.kafkaTopic() == null || appConfig.kafkaTopic().isBlank()) {
                log.warn("Kafka topic not configured. Skipping egress.");
                return;
            }

            // We use the non-blocking send and handle the result via callback
            kafkaTemplate.send(appConfig.kafkaTopic(), payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka delivery failed: {}", ex.getMessage());
                    } else {
                        log.debug("ISO Message sent to Kafka topic: {}", appConfig.kafkaTopic());
                    }
                });
        } catch (Exception e) {
            // Catching everything to ensure the pipeline continues
            log.error("Critical Kafka client failure: {}. Mapper engine remains UP.", e.getMessage());
        }
    }
}