package com.byteentropy.fix_iso20022_mapper_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(excludeName = "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
@ConfigurationPropertiesScan("com.byteentropy.fix_iso20022_mapper_core.config")
public class FixIso20022MapperCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(FixIso20022MapperCoreApplication.class, args);
    }
}