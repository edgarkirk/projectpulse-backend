ALTER TABLE projects ADD COLUMN name_normalized VARCHAR(100) AS (LOWER(name));
CREATE UNIQUE INDEX uk_projects_name_ci ON projects(name_normalized);
