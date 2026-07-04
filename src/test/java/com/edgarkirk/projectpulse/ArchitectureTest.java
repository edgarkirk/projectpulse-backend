package com.edgarkirk.projectpulse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class ArchitectureTest {

    private static final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.edgarkirk.projectpulse");

    @Test
    void controllers_must_not_access_repositories_directly() {
        assertTrue(classes.stream()
                .filter(javaClass -> javaClass.getPackageName().startsWith("com.edgarkirk.projectpulse.api"))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .noneMatch(dependency -> dependency.getTargetClass().getPackageName().startsWith("com.edgarkirk.projectpulse.persistence")));
    }

    @Test
    void controllers_must_not_access_daos_directly() {
        assertTrue(classes.stream()
                .filter(javaClass -> javaClass.getPackageName().startsWith("com.edgarkirk.projectpulse.api"))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .noneMatch(dependency -> dependency.getTargetClass().getPackageName().startsWith("com.edgarkirk.projectpulse.service.dao")));
    }

    @Test
    void services_must_not_depend_on_controllers() {
        classes.stream()
                .filter(javaClass -> javaClass.getPackageName().startsWith("com.edgarkirk.projectpulse.service"))
                .filter(javaClass -> !javaClass.getPackageName().startsWith("com.edgarkirk.projectpulse.service.exception"))
                .forEach(javaClass -> assertFalse(javaClass.getDirectDependenciesFromSelf().stream()
                        .map(dependency -> dependency.getTargetClass().getPackageName())
                        .anyMatch(packageName -> packageName.startsWith("com.edgarkirk.projectpulse.api")
                                && !packageName.startsWith("com.edgarkirk.projectpulse.api.dto"))));
    }

    @Test
    void repositories_must_not_depend_on_service_implementations() {
        assertTrue(classes.stream()
                .filter(javaClass -> javaClass.getPackageName().startsWith("com.edgarkirk.projectpulse.persistence.repository"))
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .noneMatch(dependency -> dependency.getTargetClass().getPackageName().startsWith("com.edgarkirk.projectpulse.api")
                        || dependency.getTargetClass().getPackageName().startsWith("com.edgarkirk.projectpulse.service")));
    }
}
