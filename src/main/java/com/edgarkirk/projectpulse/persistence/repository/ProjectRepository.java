package com.edgarkirk.projectpulse.persistence.repository;

import com.edgarkirk.projectpulse.domain.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Project> findByNameIgnoreCase(String name);

    List<Project> findAllByOrderByCreatedAtDesc();

    long countByStatus(ProjectStatus status);
}
