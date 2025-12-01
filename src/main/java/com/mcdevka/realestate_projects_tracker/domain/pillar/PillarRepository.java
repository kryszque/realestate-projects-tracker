package com.mcdevka.realestate_projects_tracker.domain.pillar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PillarRepository extends JpaRepository<Pillar, Long>, JpaSpecificationExecutor<Pillar> {
    boolean existsByNameAndStateAndProjectIdAndPriority(String name, String state, Long projectId, Integer priority);

    Optional<Pillar> findByIdAndStateNot(Long id, String state);
}
