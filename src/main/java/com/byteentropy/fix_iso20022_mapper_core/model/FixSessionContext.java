package com.byteentropy.fix_iso20022_mapper_core.model;

import java.time.Instant;

public record FixSessionContext(
    String sessionId,
    String senderCompId,
    String targetCompId,
    long lastSequenceNum,
    Instant connectionTime
) {
    // Helper to validate sequence continuity
    public boolean isValidSequence(long receivedSeq) {
        return receivedSeq == lastSequenceNum + 1;
    }
}