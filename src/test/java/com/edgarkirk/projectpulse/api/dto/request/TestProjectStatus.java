package com.edgarkirk.projectpulse.api.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TestProjectStatus {
    ACTIVE("Active"),
    AT_RISK("At Risk"),
    BLOCKED("Blocked"),
    ON_HOLD("On Hold");

    private final String value;

    TestProjectStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static TestProjectStatus fromValue(String value) {
        for (var status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown project status: " + value);
    }
}
