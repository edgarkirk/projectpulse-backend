# Repository Notes

- Base package: `com.edgarkirk.projectpulse`.
- Tests use H2 by default via `src/test/resources/application.yaml`.
- Flyway migration `V1__create_projects_table.sql` must keep `projects` table compatible with H2 and PostgreSQL mode.
- Project IDs are assigned in the `Project` constructor so service tests can stub repository saves without JPA generation.
- Project creation enforces case-insensitive uniqueness in both service logic and database schema.
- Required project status values: `Active`, `At Risk`, `Blocked`, `On Hold`.
- Validation errors should prefer `NotBlank` messages when multiple violations exist for the same field.
- Spring Boot 3.4 tests should use `@MockitoBean` instead of deprecated `@MockBean`.
- `@RestControllerAdvice` classes that handle validation should also carry `@Validated`.

