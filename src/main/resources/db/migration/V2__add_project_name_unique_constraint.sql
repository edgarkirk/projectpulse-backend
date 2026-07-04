alter table project add column name_ci varchar(100) as (lower(name));
create unique index uk_project_name on project (name_ci);
