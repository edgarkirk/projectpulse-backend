package com.edgarkirk.projectpulse.persistence.repository;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByNameIgnoreCase(String name);

    List<Project> findAllByOrderByCreatedAtDescIdDesc();

    long countByStatus(ProjectStatus status);
}
