package com.mcdevka.realestate_projects_tracker.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    boolean existsByNameAndPlaceAndStateAndContractorAndCompanyResposibleAndPriority(String projectName,
                                                                                     String place, String state,
                                                                                     String contractor,
                                                                                     String companyResposible,
                                                                                     Integer priority);
    List<Project> findByStateNot(String state);
}
