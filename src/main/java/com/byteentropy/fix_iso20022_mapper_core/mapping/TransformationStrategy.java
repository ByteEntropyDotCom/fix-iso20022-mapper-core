package com.byteentropy.fix_iso20022_mapper_core.mapping;

public interface TransformationStrategy {
    boolean supports(String payload);
    String toIso(String fixPayload);
    String getMarket();
}