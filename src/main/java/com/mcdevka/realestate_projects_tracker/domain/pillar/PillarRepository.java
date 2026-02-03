package com.mcdevka.realestate_projects_tracker.domain.pillar;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PillarRepository extends JpaRepository<Pillar, Long>, JpaSpecificationExecutor<Pillar> {
    boolean existsByNameAndStateAndProjectIdAndPriority(String name, String state, Long projectId, Integer priority);

    Optional<Pillar> findByIdAndStateNot(Long id, String state);

    @Modifying // Informuje Springa, że to zapytanie zmienia dane (UPDATE/DELETE)
    @Query("UPDATE Pillar p SET p.company = :company WHERE p.project.id = :projectId")
    void updateCompanyForProject(@Param("projectId") Long projectId, @Param("company") Company company);
}
