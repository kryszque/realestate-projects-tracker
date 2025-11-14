package com.mcdevka.realestate_projects_tracker.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    boolean existsByNameAndPlaceAndStateAndContractorAndCompanyResposible(String projectName,
                                                                          String place, String state,
                                                                          String contractor,
                                                                          String companyResposible);
}
