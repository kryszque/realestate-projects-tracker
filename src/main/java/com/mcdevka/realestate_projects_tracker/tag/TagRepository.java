package com.mcdevka.realestate_projects_tracker.tag;

import com.mcdevka.realestate_projects_tracker.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
}
