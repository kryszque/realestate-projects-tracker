package com.mcdevka.realestate_projects_tracker.domain.project;

import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectSearchCriteria {
    private String name;
    private String tagName;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
}
