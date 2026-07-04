package com.edgarkirk.projectpulse.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {

    ACTIVE("Active"),
    AT_RISK("At Risk"),
    BLOCKED("Blocked"),
    ON_HOLD("On Hold");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    @JsonCreator
    public static ProjectStatus fromApiValue(String value) {
        for (ProjectStatus status : values()) {
            if (status.displayName.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Project status must be one of Active, At Risk, Blocked, or On Hold.");
    }
}
