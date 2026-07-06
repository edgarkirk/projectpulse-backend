package com.edgarkirk.projectpulse.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByOrderByCreatedAtDesc();

    boolean existsByNameIgnoreCase(String name);

    long countByStatus(ProjectStatus status);
}
