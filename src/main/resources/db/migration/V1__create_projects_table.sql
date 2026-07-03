create table projects (
    id uuid not null primary key,
    name varchar(100) not null,
    owner_name varchar(100) not null,
    status varchar(20) not null,
    created_at timestamp with time zone not null,
    constraint uk_projects_name unique (name),
    constraint ck_projects_status check (status in ('Active', 'At Risk', 'Blocked', 'On Hold'))
);
