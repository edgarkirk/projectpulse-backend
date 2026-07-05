package com.edgarkirk.projectpulse.persistence.entity;

public enum ProjectStatus {
    ACTIVE("Active"),
    AT_RISK("At Risk"),
    BLOCKED("Blocked"),
    ON_HOLD("On Hold");

    private static final String INVALID_STATUS_MESSAGE =
        "Project status must be one of Active, At Risk, Blocked, or On Hold";

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ProjectStatus fromDisplayName(String displayName) {
        if (displayName == null) {
            throw new IllegalArgumentException(INVALID_STATUS_MESSAGE);
        }

        return switch (displayName) {
            case "Active" -> ACTIVE;
            case "At Risk" -> AT_RISK;
            case "Blocked" -> BLOCKED;
            case "On Hold" -> ON_HOLD;
            default -> throw new IllegalArgumentException(INVALID_STATUS_MESSAGE);
        };
    }
}
