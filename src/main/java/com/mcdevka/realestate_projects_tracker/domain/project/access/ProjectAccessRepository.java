package com.mcdevka.realestate_projects_tracker.domain.project.access;

import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectAccessRepository extends JpaRepository<ProjectAccess, Long> {
    Optional<ProjectAccess> findByUserIdAndProjectId(Long userId, Long projectId);
    List<ProjectAccess> findAllByUserId(Long userId);
}
