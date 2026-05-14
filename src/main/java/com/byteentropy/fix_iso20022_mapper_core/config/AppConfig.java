package com.byteentropy.fix_iso20022_mapper_core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

@ConfigurationProperties(prefix = "app")
public record AppConfig(
    int tcpPort, 
    String kafkaTopic, 
    String bootstrapServers,
    String market,
    Map<String, String> mappingRules
) {}