package com.mhsa.backend.auth.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleAttributeConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        return switch (dbData.toUpperCase()) {
            case "MANAGER", "PARENT" ->
                Role.PARENT;
            case "DEPENDENT", "TEEN" ->
                Role.TEEN;
            case "DOCTOR", "THERAPIST" ->
                Role.THERAPIST;
            case "ADMIN" ->
                Role.ADMIN;
            default ->
                Role.valueOf(dbData.toUpperCase());
        };
    }
}
