package com.mcdevka.realestate_projects_tracker.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    //excluding addDate and partiesInvolved (too complicated for JPA to use this simple method in
    // checking for identical projects)
    boolean existsByNameAndPlaceAndState(String projectName, String place, String state);
}
