package com.byteentropy.fix_iso20022_mapper_core.core;

import com.byteentropy.fix_iso20022_mapper_core.model.RawFixMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
class LoadTest {

    @Autowired
    private PipelineDispatcher dispatcher; // No import needed! They share the .core package.

    @Test
    void testVirtualThreadThroughput() throws InterruptedException {
        int messageCount = 10000;
        String fixMsg = "8=FIX.4.4\u000135=8\u000144=100.25\u000111=ORD123\u000110=031\u0001";
        
        System.out.println(">>> Starting Virtual Thread Load Test: " + messageCount + " messages");
        
        long start = System.currentTimeMillis();

        for (int i = 0; i < messageCount; i++) {
            dispatcher.dispatch(new RawFixMessage(fixMsg, System.nanoTime()));
        }

        long end = System.currentTimeMillis();
        long duration = end - start;
        
        System.out.println("--------------------------------------------------");
        System.out.printf("DISPATCH SUMMARY%n");
        System.out.printf("Total Messages: %d%n", messageCount);
        System.out.printf("Time Taken:     %2d ms%n", duration);
        System.out.printf("Throughput:     %.2f msg/sec (Acceptance rate)%n", 
                          (messageCount / (Math.max(duration, 1) / 1000.0)));
        System.out.println("--------------------------------------------------");

        // Let the Virtual Threads finish the work in the background
        TimeUnit.SECONDS.sleep(3);
    }
}