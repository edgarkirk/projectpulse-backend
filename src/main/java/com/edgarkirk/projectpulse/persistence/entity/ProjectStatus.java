package com.edgarkirk.projectpulse.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    ACTIVE("Active"),
    AT_RISK("At Risk"),
    BLOCKED("Blocked"),
    ON_HOLD("On Hold");

    private final String wireValue;

    ProjectStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String getWireValue() {
        return wireValue;
    }

    @JsonCreator
    public static ProjectStatus fromWireValue(String value) {
        for (ProjectStatus status : values()) {
            if (status.wireValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported project status: " + value);
    }
}
