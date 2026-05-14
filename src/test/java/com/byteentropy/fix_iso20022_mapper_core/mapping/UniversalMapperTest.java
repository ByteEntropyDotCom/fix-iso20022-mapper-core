package com.byteentropy.fix_iso20022_mapper_core.mapping;

import com.byteentropy.fix_iso20022_mapper_core.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UniversalMapperTest {

    private UniversalMessageMapper mapper;
    private AppConfig appConfig;

    private final String fixExecutionReport = "8=FIX.4.4\u000135=8\u000144=100.25\u000110=031\u0001";

    @BeforeEach
    void setUp() {
        appConfig = Mockito.mock(AppConfig.class);
        mapper = new UniversalMessageMapper(appConfig);
    }

    @Test
    @DisplayName("Verify Singapore MEPS+ Mapping")
    void testSingaporeMapping() {
        when(appConfig.market()).thenReturn("SG");
        when(appConfig.mappingRules()).thenReturn(Map.of("r1", "35=8=execution-report"));

        String xml = mapper.toIso(fixExecutionReport);

        assertAll("Singapore Assertions",
            () -> assertTrue(xml.contains("sese.023.SG"), "Namespace mismatch"),
            () -> assertTrue(xml.contains("SG_MAS_"), "MsgId prefix mismatch"),
            () -> assertTrue(xml.contains("SG_Specific_Block"), "Market block mismatch"),
            () -> assertTrue(xml.contains("MEPS_PLUS_Ref"), "MEPS ref mismatch"),
            () -> assertTrue(xml.contains("SG_TIMESTAMP"), "Timestamp placeholder mismatch")
        );
    }

    @Test
    @DisplayName("Verify Japan Zengin Mapping")
    void testJapanMapping() {
        when(appConfig.market()).thenReturn("JP");
        when(appConfig.mappingRules()).thenReturn(Map.of("r1", "35=8=execution-report"));

        String xml = mapper.toIso(fixExecutionReport);

        assertAll("Japan Assertions",
            () -> assertTrue(xml.contains("sese.023.JP")),
            () -> assertTrue(xml.contains("ZenginHeader")),
            () -> assertTrue(xml.contains("JP_BOJ_"))
        );
    }

    @Test
    @DisplayName("Verify UK CHAPS Mapping")
    void testUkMapping() {
        when(appConfig.market()).thenReturn("UK");
        when(appConfig.mappingRules()).thenReturn(Map.of("r1", "35=8=execution-report"));

        String xml = mapper.toIso(fixExecutionReport);

        assertAll("UK Assertions",
            () -> assertTrue(xml.contains("sese.023.UK")),
            () -> assertTrue(xml.contains("CHAPS_Extension")),
            () -> assertTrue(xml.contains("UK_BOE_"))
        );
    }

    @Test
    @DisplayName("Verify USA FedWire Mapping")
    void testUsaMapping() {
        when(appConfig.market()).thenReturn("USA");
        when(appConfig.mappingRules()).thenReturn(Map.of("r1", "35=8=execution-report"));

        String xml = mapper.toIso(fixExecutionReport);

        assertAll("USA Assertions",
            () -> assertTrue(xml.contains("sese.023.USA")),
            () -> assertTrue(xml.contains("FedWireHeader")),
            () -> assertTrue(xml.contains("FED_WIRE_ID_123"))
        );
    }
}