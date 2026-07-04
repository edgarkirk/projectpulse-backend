create table projects (
    id uuid primary key,
    name varchar(100) not null unique,
    owner_name varchar(100) not null,
    status varchar(20) not null,
    created_at timestamp with time zone not null
);
