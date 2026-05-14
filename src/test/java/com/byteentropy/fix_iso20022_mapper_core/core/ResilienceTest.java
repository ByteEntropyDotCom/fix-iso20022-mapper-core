package com.byteentropy.fix_iso20022_mapper_core.core;

import com.byteentropy.fix_iso20022_mapper_core.config.AppConfig;
import com.byteentropy.fix_iso20022_mapper_core.infra.KafkaEgressClient;
import com.byteentropy.fix_iso20022_mapper_core.infra.TelemetryService;
import com.byteentropy.fix_iso20022_mapper_core.mapping.TransformationStrategy;
import com.byteentropy.fix_iso20022_mapper_core.model.RawFixMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilienceTest {

    @Mock private KafkaEgressClient kafkaClient;
    @Mock private TransformationStrategy strategy;
    @Mock private TelemetryService telemetry;
    @Mock private AppConfig appConfig;

    private MessageTransformer transformer;

    @BeforeEach
    void setUp() {
        // We inject the mocked strategy into the transformer
        transformer = new MessageTransformer(List.of(strategy), kafkaClient, telemetry, appConfig);
    }

    @Test
    @DisplayName("Pipeline should record metrics even if Kafka fails")
    void testKafkaFailureResilience() {
        // Arrange
        RawFixMessage msg = new RawFixMessage("8=FIX.4.4|35=8|", System.nanoTime());
        
        // Mocking the strategy and config to ensure the strategy is selected
        when(appConfig.market()).thenReturn("USA");
        when(strategy.getMarket()).thenReturn("USA");
        when(strategy.supports(anyString())).thenReturn(true);
        when(strategy.toIso(anyString())).thenReturn("<ISO20022_XML_DOCUMENT/>");
        
        // Simulate a Kafka crash/timeout
        doThrow(new RuntimeException("Kafka Broker Unavailable"))
                .when(kafkaClient).send(anyString());

    // Act
    transformer.transformAndSend(msg);

    // Assert
    // 1. Verify the mapping was actually attempted
    verify(strategy, times(1)).toIso(anyString());
    
    // 2. Critical: Verify telemetry was recorded even though an exception occurred in the pipeline
    // This proves the 'finally' block in MessageTransformer is working correctly.
    verify(telemetry, times(1)).recordLatency(anyLong());
    
    // 3. Ensure the exception was caught and didn't crash the thread (implicitly confirmed by reaching this line)
    }

    @Test
    @DisplayName("Pipeline should handle cases where no strategy is found gracefully")
    void testNoStrategyFoundResilience() {
        RawFixMessage msg = new RawFixMessage("8=FIX.4.4|35=UNKNOWN|", System.nanoTime());
        
        when(appConfig.market()).thenReturn("USA");
        when(strategy.supports(anyString())).thenReturn(false); // No strategy supports this payload

        transformer.transformAndSend(msg);

        // Verify: It shouldn't attempt to map or send to Kafka, but should still record telemetry/latency
        verify(strategy, never()).toIso(anyString());
        verify(kafkaClient, never()).send(anyString());
        verify(telemetry, times(1)).recordLatency(anyLong());
    }
}