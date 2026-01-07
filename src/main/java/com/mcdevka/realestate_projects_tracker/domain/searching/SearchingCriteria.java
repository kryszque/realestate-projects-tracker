package com.mcdevka.realestate_projects_tracker.domain.searching;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SearchingCriteria {
    private String name;
    private List<String> tags;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
    private Integer priority;

    private Long projectId;
    private Long pillarId;
}
