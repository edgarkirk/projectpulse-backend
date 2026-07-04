package com.edgarkirk.projectpulse.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ProjectRepositoryTest {

    private static final String PROJECT_ENTITY_FQCN = "com.edgarkirk.projectpulse.persistence.entity.Project";
    private static final String PROJECT_REPOSITORY_FQCN = "com.edgarkirk.projectpulse.persistence.repository.ProjectRepository";

    @Test
    void should_define_projectEntity_fields_exactly() {
        Class<?> projectType = requireClass(PROJECT_ENTITY_FQCN);

        assertThat(projectType.getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .containsExactlyInAnyOrder("id", "name", "ownerName", "status", "createdAt");
    }

    @Test
    void should_expose_caseInsensitive_existsByName_lookup() {
        Class<?> repositoryType = requireClass(PROJECT_REPOSITORY_FQCN);
        Method method = requireMethod(repositoryType, "existsByNameIgnoreCase", String.class);

        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    void should_expose_createdAt_descending_listing_query() {
        Class<?> repositoryType = requireClass(PROJECT_REPOSITORY_FQCN);
        Method method = requireMethod(repositoryType, "findAllByOrderByCreatedAtDesc");

        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    void should_expose_projectLookup_byId() {
        Class<?> repositoryType = requireClass(PROJECT_REPOSITORY_FQCN);
        Method method = requireMethod(repositoryType, "findById", Object.class);

        assertThat(method.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    void should_expose_statusCounting_query() {
        Class<?> repositoryType = requireClass(PROJECT_REPOSITORY_FQCN);
        Class<?> statusType = resolveStatusType();
        Method method = requireMethod(repositoryType, "countByStatus", statusType);

        assertThat(method.getReturnType()).isIn(long.class, Long.class);
    }

    private static Class<?> resolveStatusType() {
        for (String candidate : List.of(
                "com.edgarkirk.projectpulse.persistence.entity.ProjectStatus",
                "com.edgarkirk.projectpulse.api.dto.request.ProjectStatus")) {
            try {
                return Class.forName(candidate);
            } catch (ClassNotFoundException ignored) {
                // Try next candidate.
            }
        }
        throw new AssertionError("No expected project status type was found.");
    }

    private static Class<?> requireClass(String fqcn) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("Expected class not found: " + fqcn, exception);
        }
    }

    private static Method requireMethod(Class<?> type, String methodName, Class<?>... parameterTypes) {
        try {
            return type.getMethod(methodName, parameterTypes);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Expected method not found: " + type.getName() + '.' + methodName, exception);
        }
    }
}
