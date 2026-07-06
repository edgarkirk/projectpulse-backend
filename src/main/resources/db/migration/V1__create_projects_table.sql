CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_projects_name UNIQUE (name)
);
