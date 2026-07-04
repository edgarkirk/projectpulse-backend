package com.edgarkirk.projectpulse.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByNameIgnoreCase(String name);

    @Query("select project from Project project order by project.createdAt desc, project.id desc")
    List<Project> findAllByOrderByCreatedAtDesc();

    long countByStatus(ProjectStatus status);
}
