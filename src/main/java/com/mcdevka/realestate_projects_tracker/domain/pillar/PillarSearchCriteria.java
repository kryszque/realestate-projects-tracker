package com.mcdevka.realestate_projects_tracker.domain.pillar;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PillarSearchCriteria {
    private String name;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
}
