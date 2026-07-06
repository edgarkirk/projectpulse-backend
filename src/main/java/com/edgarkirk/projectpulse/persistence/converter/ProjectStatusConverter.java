package com.edgarkirk.projectpulse.persistence.converter;

import com.edgarkirk.projectpulse.api.dto.request.ProjectStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProjectStatusConverter implements AttributeConverter<ProjectStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProjectStatus attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ProjectStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ProjectStatus.fromValue(dbData);
    }
}
