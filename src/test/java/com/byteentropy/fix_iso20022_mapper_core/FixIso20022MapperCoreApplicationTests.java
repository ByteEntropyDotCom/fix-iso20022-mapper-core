package com.byteentropy.fix_iso20022_mapper_core;

import com.byteentropy.fix_iso20022_mapper_core.engine.NettyTcpServer;
import com.byteentropy.fix_iso20022_mapper_core.core.PipelineDispatcher;
import com.byteentropy.fix_iso20022_mapper_core.mapping.UniversalMessageMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class FixIso20022MapperCoreApplicationTests {

    @Autowired
    private NettyTcpServer nettyTcpServer;

    @Autowired
    private PipelineDispatcher pipelineDispatcher;

    @Autowired
    private UniversalMessageMapper universalMessageMapper;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void contextLoads() {
        assertNotNull(nettyTcpServer, "Netty Server should be active");
        assertNotNull(pipelineDispatcher, "Pipeline Dispatcher should be active");
        assertNotNull(universalMessageMapper, "Universal Mapper should be active");
    }
}