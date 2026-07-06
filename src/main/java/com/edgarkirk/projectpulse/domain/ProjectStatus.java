package com.edgarkirk.projectpulse.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    ACTIVE("Active"),
    AT_RISK("At Risk"),
    BLOCKED("Blocked"),
    ON_HOLD("On Hold");

    private final String apiValue;

    ProjectStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    @JsonValue
    public String getApiValue() {
        return apiValue;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ProjectStatus fromJson(String value) {
        for (ProjectStatus status : values()) {
            if (status.apiValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("status must be one of Active, At Risk, Blocked, On Hold");
    }
}
