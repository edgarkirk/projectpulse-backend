package com.edgarkirk.projectpulse.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.persistence.entity.TestProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestProjectRepository extends JpaRepository<TestProjectEntity, UUID> {

    List<TestProjectEntity> findAllByOrderByCreatedAtDesc();

    boolean existsByNameIgnoreCase(String name);

    long countByStatus(String status);
}
