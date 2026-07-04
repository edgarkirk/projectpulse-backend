package com.edgarkirk.projectpulse.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edgarkirk.projectpulse.persistence.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByOrderByCreatedAtDesc();

    @Query("select p from Project p where lower(p.name) = lower(:name)")
    Optional<Project> findByNameIgnoreCase(@Param("name") String name);

    boolean existsByNameIgnoreCase(String name);

    long countByStatus(String status);
}
