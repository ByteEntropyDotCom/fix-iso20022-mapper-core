package com.byteentropy.fix_iso20022_mapper_core.model;

public record RawFixMessage(String payload, long ingressTimestamp) {}