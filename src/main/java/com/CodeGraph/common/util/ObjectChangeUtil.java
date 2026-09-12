package com.CodeGraph.common.util;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ObjectChangeUtil {

    private final ObjectMapper objectMapper;

    public ObjectChangeUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getChangedFields(
            Object existingObject,
            Object requestObject) {

        Map<String, Object> changedFields =
                new LinkedHashMap<>();

        for (Field requestField :
                requestObject.getClass().getDeclaredFields()) {

            String fieldName = requestField.getName();

            try {
                requestField.setAccessible(true);

                Object requestValue =
                        requestField.get(requestObject);

                // Field was not sent by frontend
                if (requestValue == null) {
                    continue;
                }

                Field existingField =
                        existingObject
                                .getClass()
                                .getDeclaredField(fieldName);

                existingField.setAccessible(true);

                Object existingValue =
                        existingField.get(existingObject);

                Object newValue =
                        convertValue(
                                requestValue,
                                existingField.getType()
                        );

                if (!Objects.equals(
                        existingValue,
                        newValue)) {

                    changedFields.put(
                            fieldName,
                            newValue
                    );
                }

            } catch (NoSuchFieldException ignored) {

                // Request field doesn't exist
                // in the entity.

            } catch (IllegalAccessException e) {

                throw new IllegalStateException(
                        "Unable to compare field: "
                                + fieldName,
                        e
                );
            }
        }

        return changedFields;
    }

    private Object convertValue(
            Object value,
            Class<?> targetType) {

        if (!(value instanceof JsonNode node)) {
            return value;
        }

        if (node.isNull()) {
            return null;
        }

        // Blank string means clear the field
        if (node.isTextual()
                && node.asString().isBlank()) {

            return null;
        }

        return objectMapper.convertValue(
                node,
                targetType
        );
    }
}