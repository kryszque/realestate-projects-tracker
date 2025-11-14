package com.mcdevka.realestate_projects_tracker.domain.searching;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SearchingCriteria {
    private String name;
    private String tagName;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
}
