create table projects (
    id uuid not null primary key,
    name varchar(100) not null,
    owner_name varchar(100) not null,
    status varchar(20) not null,
    created_at timestamp with time zone not null,
    name_lower varchar(100) generated always as (lower(name)),
    constraint uq_projects_name unique (name),
    constraint uq_projects_name_lower unique (name_lower)
);
