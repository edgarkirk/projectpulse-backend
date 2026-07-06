package com.edgarkirk.projectpulse.persistence.repository;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByOrderByCreatedAtDesc();

    boolean existsByNameIgnoreCase(String name);

    long countByStatus(ProjectStatus status);
}
