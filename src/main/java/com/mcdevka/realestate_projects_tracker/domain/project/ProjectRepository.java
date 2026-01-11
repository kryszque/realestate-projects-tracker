package com.mcdevka.realestate_projects_tracker.domain.project;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    boolean existsByNameAndPersonResponsibleAndStateAndDeadlineAndCompanyResposibleAndPriority(String projectName,
                                                                                     String personResponsible, String state,
                                                                                     LocalDate deadline,
                                                                                     Company companyResposible,
                                                                                     Integer priority);
    List<Project> findByStateNot(String state);
    List<Project> findByStateNotAndCompanyResposibleIn(String state, Collection<Company> companyResposible);
}
