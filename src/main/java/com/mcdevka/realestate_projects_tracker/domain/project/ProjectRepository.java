package com.mcdevka.realestate_projects_tracker.domain.project;

import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    boolean existsByNameAndPersonResponsibleAndStateAndDeadlineAndCompanyResposibleAndPriority(String projectName,
                                                                                     String personResponsible, String state,
                                                                                     LocalDate deadline,
                                                                                     String companyResposible,
                                                                                     Integer priority);
    List<Project> findByStateNot(String state);
    List<Project> findByStateNotAndCompanyResposible(String state, String companyResposible);
}
