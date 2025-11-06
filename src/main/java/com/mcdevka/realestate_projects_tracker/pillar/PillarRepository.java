package com.mcdevka.realestate_projects_tracker.pillar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PillarRepository extends JpaRepository<Pillar, Long> {
    boolean existsByNameAndStatusAndProjectId(String name, String status, Long projectId);
}
