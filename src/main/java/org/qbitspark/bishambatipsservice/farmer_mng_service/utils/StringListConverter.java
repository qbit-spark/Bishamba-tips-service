package org.qbitspark.bishambatipsservice.farmer_mng_service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Converter
@Slf4j
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private final ObjectMapper objectMapper;

    public StringListConverter() {
        this.objectMapper = new ObjectMapper();

        // Configure for LocalDateTime support (if needed in future)
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ignore unknown properties for backward compatibility
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }

        try {
            String json = objectMapper.writeValueAsString(attribute);
            log.debug("Converting String list to JSON: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Error converting String list to JSON", e);
            return "[]";
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || "null".equals(dbData)) {
            return new ArrayList<>();
        }

        try {
            List<String> result = objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
            log.debug("Converting JSON to String list: {} items", result != null ? result.size() : 0);
            return result != null ? result : new ArrayList<>();
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to String list: {}", dbData, e);
            return new ArrayList<>();
        }
    }
}