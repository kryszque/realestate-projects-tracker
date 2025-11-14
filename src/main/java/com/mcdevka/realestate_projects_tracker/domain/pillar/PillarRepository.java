package com.mcdevka.realestate_projects_tracker.domain.pillar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PillarRepository extends JpaRepository<Pillar, Long>, JpaSpecificationExecutor<Pillar> {
    boolean existsByNameAndStateAndProjectId(String name, String state, Long projectId);
}
