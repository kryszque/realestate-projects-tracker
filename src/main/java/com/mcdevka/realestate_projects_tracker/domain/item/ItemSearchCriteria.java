package com.mcdevka.realestate_projects_tracker.domain.item;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ItemSearchCriteria {
    private String name;
    private String tagName;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
}
