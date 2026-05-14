package com.byteentropy.fix_iso20022_mapper_core.core;

import com.byteentropy.fix_iso20022_mapper_core.model.RawFixMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyTest {

    @Autowired 
    private PipelineDispatcher dispatcher;

    @MockitoBean 
    private MessageTransformer transformer;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @DisplayName("Verify Virtual Threads handle 1,000 message burst")
    void testHighVolumeBurstHandling() throws InterruptedException {
        int messageCount = 1000;
        CountDownLatch latch = new CountDownLatch(messageCount);

        // Mock the transformer to just count down the latch
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(transformer).transformAndSend(any());

        for (int i = 0; i < messageCount; i++) {
            dispatcher.dispatch(new RawFixMessage("8=FIX.4.4|35=8|", System.nanoTime()));
        }

        boolean allProcessed = latch.await(10, TimeUnit.SECONDS);
        assertTrue(allProcessed, "Should process 1,000 messages via virtual threads within 10s");
    }
}