package com.edgarkirk.projectpulse.persistence.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ProjectStatusConverter implements AttributeConverter<ProjectStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProjectStatus attribute) {
        return attribute == null ? null : attribute.getWireValue();
    }

    @Override
    public ProjectStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ProjectStatus.fromWireValue(dbData);
    }
}
