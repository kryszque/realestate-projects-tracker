package com.mcdevka.realestate_projects_tracker.domain.pillar;

import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PillarRepository extends JpaRepository<Pillar, Long>, JpaSpecificationExecutor<Pillar> {
    boolean existsByNameAndStateAndProjectId(String name, String state, Long projectId);

    Optional<Pillar> findByIdAndStateNot(Long id, String state);
}
