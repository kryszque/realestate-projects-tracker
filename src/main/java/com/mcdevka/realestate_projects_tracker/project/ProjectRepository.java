package com.mcdevka.realestate_projects_tracker.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    //excluding addDate in checking for identical projects
    boolean existsByNameAndPlaceAndPartiesInvolvedAndState(String projectName, String place,
                                                           List<String> parties, String state);
}
