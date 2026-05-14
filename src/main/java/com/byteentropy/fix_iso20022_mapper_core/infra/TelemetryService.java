package com.byteentropy.fix_iso20022_mapper_core.infra;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

@Service
public class TelemetryService {

    private final MeterRegistry meterRegistry;

    // Use Optional to allow the bean to be missing in tests
    public TelemetryService(Optional<MeterRegistry> meterRegistry) {
        this.meterRegistry = meterRegistry.orElse(null);
    }

    public void recordLatency(long nanos) {
        if (meterRegistry != null) {
            Timer.builder("fix.mapper.latency")
                 .description("Latency of FIX to ISO conversion")
                 .register(meterRegistry)
                 .record(nanos, TimeUnit.NANOSECONDS);
        }
    }
}