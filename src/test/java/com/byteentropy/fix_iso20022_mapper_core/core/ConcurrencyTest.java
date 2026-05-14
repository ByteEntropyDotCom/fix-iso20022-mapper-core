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

        // FIX: Detect if we are on GitHub Actions and give it 30s instead of 10s
        boolean isCI = "true".equalsIgnoreCase(System.getenv("GITHUB_ACTIONS"));
        int waitTime = isCI ? 30 : 10;

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(transformer).transformAndSend(any());

        for (int i = 0; i < messageCount; i++) {
            dispatcher.dispatch(new RawFixMessage("8=FIX.4.4|35=8|", System.nanoTime()));
        }

        // Use the dynamic wait time
        boolean allProcessed = latch.await(waitTime, TimeUnit.SECONDS);
        assertTrue(allProcessed, "Processed " + (messageCount - latch.getCount()) + " / " + messageCount);
    }
}