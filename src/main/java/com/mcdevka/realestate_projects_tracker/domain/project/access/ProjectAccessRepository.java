package com.mcdevka.realestate_projects_tracker.domain.project.access;

import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProjectAccessRepository extends JpaRepository<ProjectAccess, Long> {
    Optional<ProjectAccess> findByUserIdAndProjectId(Long userId, Long projectId);
    List<ProjectAccess> findAllByUserId(Long userId);
    void deleteByUserIdAndProjectId(Long userId, Long projectId);
    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectAccess pa WHERE pa.user.id = :userId AND pa.project.id IN :projectIds")
    void deleteAllByUserIdAndProjectsIds(@Param("userId") Long userId, @Param("projectIds") List<Long> projectIds);
}
