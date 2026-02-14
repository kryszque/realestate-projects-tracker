package com.mcdevka.realestate_projects_tracker.domain.project;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    boolean existsByNameAndPersonResponsibleAndStateAndDeadlineAndCompanyAndPriority(
            String name,
            String personResponsible,
            String state,
            LocalDate deadline,
            Company company,
            Integer priority
    );
    List<Project> findByStateNot(String state);
    // Znajdź projekty należące do jednej z wielu firm (IN clause)
    List<Project> findByStateNotAndCompanyIn(String state, Collection<Company> companies);

    // Znajdź projekty dla jednej konkretnej firmy (do usuwania uprawnień)
    List<Project> findByStateNotAndCompany(String state, Company company);

    @Query("SELECT p FROM Project p WHERE " +
            "(:#{#criteria.name} IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#criteria.name}, '%'))) " +
            // ... inne warunki (tags, dates, etc) ...
            "AND (:#{#criteria.userAllowedCompanyIds} IS NULL OR p.company.id IN :#{#criteria.userAllowedCompanyIds})")
    List<Project> searchProjects(@Param("criteria") SearchingCriteria criteria);
}
