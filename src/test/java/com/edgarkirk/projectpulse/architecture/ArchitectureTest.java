package com.edgarkirk.projectpulse.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.edgarkirk.projectpulse";
    private static final String API_PACKAGE = BASE_PACKAGE + ".api";
    private static final String DTO_PACKAGE = BASE_PACKAGE + ".api.dto";
    private static final String SERVICE_PACKAGE = BASE_PACKAGE + ".service";
    private static final String EXCEPTION_PACKAGE = BASE_PACKAGE + ".service.exception";
    private static final String PERSISTENCE_REPOSITORY_PACKAGE = BASE_PACKAGE + ".persistence.repository";
    private static final String PERSISTENCE_PACKAGE = BASE_PACKAGE + ".persistence";

    private static final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

    @Test
    void controllers_must_not_access_repositories_directly() {
        for (JavaClass javaClass : classes) {
            if (javaClass.getPackageName().startsWith(API_PACKAGE) && !javaClass.getPackageName().startsWith(DTO_PACKAGE)) {
                assertThat(javaClass.getDirectDependenciesFromSelf())
                        .noneMatch(dependency -> dependency.getTargetClass().getPackageName().startsWith(PERSISTENCE_PACKAGE));
            }
        }
    }

    @Test
    void services_must_not_depend_on_controllers() {
        for (JavaClass javaClass : classes) {
            if (javaClass.getPackageName().startsWith(SERVICE_PACKAGE)
                    && !javaClass.getPackageName().startsWith(EXCEPTION_PACKAGE)) {
                assertThat(javaClass.getDirectDependenciesFromSelf())
                        .noneMatch(dependency -> {
                            String targetPackage = dependency.getTargetClass().getPackageName();
                            return targetPackage.startsWith(API_PACKAGE) && !targetPackage.startsWith(DTO_PACKAGE);
                        });
            }
        }
    }

    @Test
    void repositories_must_not_depend_on_services_or_controllers() {
        for (JavaClass javaClass : classes) {
            if (javaClass.getPackageName().startsWith(PERSISTENCE_REPOSITORY_PACKAGE)) {
                assertThat(javaClass.getDirectDependenciesFromSelf())
                        .noneMatch(dependency -> {
                            String targetPackage = dependency.getTargetClass().getPackageName();
                            return targetPackage.startsWith(SERVICE_PACKAGE) || targetPackage.startsWith(API_PACKAGE);
                        });
            }
        }
    }
}
