package com.byteentropy.fix_iso20022_mapper_core.mapping;

import com.byteentropy.fix_iso20022_mapper_core.config.AppConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import quickfix.DataDictionary;
import quickfix.Message;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class UniversalMessageMapper implements TransformationStrategy {

    private final AppConfig appConfig;
    private final DataDictionary dictionary;
    private final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    public UniversalMessageMapper(AppConfig appConfig) {
        this.appConfig = appConfig;
        try {
            // Ensure FIX44.xml is in src/main/resources
            this.dictionary = new DataDictionary("FIX44.xml");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load FIX Dictionary", e);
        }
    }

    @Override
    public boolean supports(String payload) {
        if (appConfig.mappingRules() == null) return false;
        return appConfig.mappingRules().values().stream()
                .anyMatch(rule -> rule.contains("=") && payload.contains(rule.substring(0, rule.lastIndexOf("="))));
    }

    @Override
    public String getMarket() {
        return appConfig.market();
    }

    @Override
    public String toIso(String fixPayload) {
        try {
            // 1. QuickFIX/J Parsing
            Message fixMsg = new Message(fixPayload, dictionary, false);
            String msgType = fixMsg.getHeader().getString(35);
            
            // 2. Resolve Template
            String mappingRule = appConfig.mappingRules().values().stream()
                    .filter(rule -> rule.startsWith("35=" + msgType))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No rule for MsgType: " + msgType));
            
            String templateName = mappingRule.substring(mappingRule.lastIndexOf("=") + 1);
            String path = String.format("templates/%s/%s.xml", appConfig.market(), templateName);
            String template = new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);

            // 3. Dynamic Replacement
            String ref = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String resultXml = template
                    .replace("{{REF}}", ref)
                    .replace("{{PAYLOAD}}", fixPayload.replace('\u0001', '|'))
                    .replace("{{MARKET}}", appConfig.market());

            // 4. XSD Validation (if XSD exists)
            validateIfPresent(resultXml, appConfig.market(), templateName);

            return resultXml;
        } catch (Exception e) {
            throw new RuntimeException("Mapper Error [" + appConfig.market() + "]: " + e.getMessage(), e);
        }
    }

    private void validateIfPresent(String xml, String market, String template) {
        try {
            String xsdPath = String.format("xsd/%s/%s.xsd", market, template);
            ClassPathResource res = new ClassPathResource(xsdPath);
            if (res.exists()) {
                Schema schema = schemaFactory.newSchema(res.getURL());
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(new StringReader(xml)));
            }
        } catch (Exception e) {
            throw new RuntimeException("XSD Validation Failed: " + e.getMessage(), e);
        }
    }
}