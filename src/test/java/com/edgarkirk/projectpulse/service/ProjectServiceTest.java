package com.edgarkirk.projectpulse.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    private static final String SERVICE_FQCN = "com.edgarkirk.projectpulse.service.ProjectService";
    private static final String REPOSITORY_FQCN = "com.edgarkirk.projectpulse.persistence.repository.ProjectRepository";
    private static final String ENTITY_FQCN = "com.edgarkirk.projectpulse.persistence.entity.Project";
    private static final String CREATE_REQUEST_FQCN = "com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest";
    private static final String PROJECT_RESPONSE_FQCN = "com.edgarkirk.projectpulse.api.dto.response.ProjectResponse";
    private static final String DASHBOARD_SUMMARY_FQCN = "com.edgarkirk.projectpulse.api.dto.response.DashboardSummary";

    @Test
    void should_inject_repository_through_constructor() {
        Class<?> repositoryType = requireClass(REPOSITORY_FQCN);
        Object repositoryMock = Mockito.mock(repositoryType, invocation -> defaultReturnValue(invocation.getMethod().getReturnType()));
        Class<?> serviceType = requireClass(SERVICE_FQCN);

        Object service = instantiate(serviceType, new Class<?>[] {repositoryType}, repositoryMock);

        assertThat(service).isNotNull();
    }

    @Test
    void should_createProject_whenNameIsUnique() {
        Object service = instantiateService(repositoryAnswerContext(false, null, null, null));
        Object request = instantiateRequest("Atlas Migration", "Jane Doe", "Active");

        Object response = invokeFirstMatchingMethod(service,
                List.of("createProject"),
                new Object[] {request},
                requireClass(PROJECT_RESPONSE_FQCN));

        assertThat(readProperty(response, "name")).isEqualTo("Atlas Migration");
        assertThat(readProperty(response, "ownerName")).isEqualTo("Jane Doe");
        assertThat(readProperty(response, "status")).isEqualTo("Active");
        assertThat(readProperty(response, "id")).isInstanceOf(UUID.class);
        assertThat(readProperty(response, "createdAt")).isNotNull();
    }

    @Test
    void should_rejectDuplicateProjectName_whenCaseInsensitiveMatchExists() {
        Object service = instantiateService(repositoryAnswerContext(true, null, null, null));
        Object request = instantiateRequest("Atlas Migration", "Jane Doe", "Active");

        try {
            invokeFirstMatchingMethod(service, List.of("createProject"), new Object[] {request}, requireClass(PROJECT_RESPONSE_FQCN));
            throw new AssertionError("Expected duplicate project name failure.");
        } catch (RuntimeException exception) {
            assertThat(exception).hasMessageContaining("Atlas Migration");
        }
    }

    @Test
    void should_returnProject_whenIdExists() {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Object entity = newProjectEntity(id, "Atlas Migration", "Jane Doe", "Active", Instant.parse("2026-07-04T20:00:00Z"));
        Object service = instantiateService(repositoryAnswerContext(false, entity, List.of(entity), Map.of("Active", 1L)));

        Object response = invokeFirstMatchingMethod(service,
                List.of("getProjectById", "getProject"),
                new Object[] {id},
                requireClass(PROJECT_RESPONSE_FQCN));

        assertThat(readProperty(response, "id")).isEqualTo(id);
        assertThat(readProperty(response, "name")).isEqualTo("Atlas Migration");
    }

    @Test
    void should_buildDashboardSummary_fromRepositoryCounts() {
        Object service = instantiateService(repositoryAnswerContext(false, null, List.of(), Map.of(
                "Active", 3L,
                "At Risk", 2L,
                "Blocked", 1L,
                "On Hold", 0L)));

        Object summary = invokeFirstMatchingMethod(service,
                List.of("getDashboardSummary", "dashboardSummary", "summary"),
                new Object[] {},
                requireClass(DASHBOARD_SUMMARY_FQCN));

        assertThat(readProperty(summary, "totalProjects")).isEqualTo(6);
        assertThat(readProperty(summary, "active")).isEqualTo(3);
        assertThat(readProperty(summary, "atRisk")).isEqualTo(2);
        assertThat(readProperty(summary, "blocked")).isEqualTo(1);
        assertThat(readProperty(summary, "onHold")).isEqualTo(0);
    }

    private static Object instantiateService(InvocationContext context) {
        Class<?> repositoryType = requireClass(REPOSITORY_FQCN);
        Class<?> serviceType = requireClass(SERVICE_FQCN);
        Object repositoryMock = Mockito.mock(repositoryType, invocation -> context.answer(invocation));
        return instantiate(serviceType, new Class<?>[] {repositoryType}, repositoryMock);
    }

    private static InvocationContext repositoryAnswerContext(boolean duplicateExists, Object projectById, List<Object> orderedProjects, Map<String, Long> counts) {
        return new InvocationContext(duplicateExists, projectById, orderedProjects, counts);
    }

    private static Object invokeFirstMatchingMethod(Object target, List<String> methodNames, Object[] args, Class<?> expectedReturnType) {
        Method method = null;
        for (String methodName : methodNames) {
            for (Method candidate : target.getClass().getMethods()) {
                if (candidate.getName().equals(methodName) && candidate.getParameterCount() == args.length) {
                    method = candidate;
                    break;
                }
            }
            if (method != null) {
                break;
            }
        }

        if (method == null) {
            throw new AssertionError("Expected one of methods " + methodNames + " on " + target.getClass().getName());
        }
        assertThat(method.getReturnType()).isEqualTo(expectedReturnType);

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to invoke method " + method.getName(), exception);
        }
    }

    private static Object instantiateRequest(String name, String ownerName, String status) {
        Class<?> requestType = requireClass(CREATE_REQUEST_FQCN);
        try {
            if (requestType.isRecord()) {
                Constructor<?> canonicalConstructor = requestType.getDeclaredConstructor(String.class, String.class, String.class);
                return canonicalConstructor.newInstance(name, ownerName, status);
            }
            Object request = requestType.getDeclaredConstructor().newInstance();
            setField(request, "name", name);
            setField(request, "ownerName", ownerName);
            setField(request, "status", status);
            return request;
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to instantiate request DTO " + CREATE_REQUEST_FQCN, exception);
        }
    }

    private static Object newProjectEntity(UUID id, String name, String ownerName, String status, Instant createdAt) {
        Class<?> entityType = requireClass(ENTITY_FQCN);
        try {
            Constructor<?> constructor = entityType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object entity = constructor.newInstance();
            setField(entity, "id", id);
            setField(entity, "name", name);
            setField(entity, "ownerName", ownerName);
            setField(entity, "status", adaptStatusValue(entityType, status));
            setField(entity, "createdAt", createdAt);
            return entity;
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to instantiate entity " + ENTITY_FQCN, exception);
        }
    }

    private static Object adaptStatusValue(Class<?> entityType, String status) throws ReflectiveOperationException {
        for (Field field : entityType.getDeclaredFields()) {
            if (field.getName().equals("status") && field.getType().isEnum()) {
                @SuppressWarnings("unchecked")
                Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType().asSubclass(Enum.class);
                return Enum.valueOf(enumType, toEnumConstant(status));
            }
        }
        return status;
    }

    private static String toEnumConstant(String status) {
        return status.toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private static Object readProperty(Object target, String propertyName) {
        if (target == null) {
            return null;
        }
        Class<?> targetType = target.getClass();

        try {
            Method accessor = targetType.getMethod(propertyName);
            return accessor.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            // Fall through to field lookup.
        }

        try {
            Field field = targetType.getDeclaredField(propertyName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to read property '" + propertyName + "' from " + targetType.getName(), exception);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Class<?> requireClass(String fqcn) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("Expected class not found: " + fqcn, exception);
        }
    }

    private static Object instantiate(Class<?> type, Class<?>[] parameterTypes, Object... args) {
        try {
            Constructor<?> constructor = type.getConstructor(parameterTypes);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to instantiate " + type.getName(), exception);
        }
    }

    private static Object defaultReturnValue(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }

    private record InvocationContext(boolean duplicateExists, Object projectById, List<Object> orderedProjects, Map<String, Long> counts) {

        private Object answer(org.mockito.invocation.InvocationOnMock invocation) {
            String methodName = invocation.getMethod().getName();
            return switch (methodName) {
                case "existsByNameIgnoreCase" -> duplicateExists;
                case "save" -> invocation.getArgument(0);
                case "findById" -> Optional.ofNullable(projectById);
                case "findAllByOrderByCreatedAtDesc" -> orderedProjects;
                case "countByStatus" -> counts.getOrDefault(String.valueOf((Object) invocation.getArgument(0)), 0L);
                default -> defaultReturnValue(invocation.getMethod().getReturnType());
            };
        }
    }
}
