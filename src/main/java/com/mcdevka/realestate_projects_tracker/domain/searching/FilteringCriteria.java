package com.mcdevka.realestate_projects_tracker.domain.searching;

import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import lombok.Data;

import java.util.List;

@Data
public class FilteringCriteria {
    private boolean filterByProject = true;
    private boolean filterByPillar = true;
    private boolean filterByItem = true;
    private List<String> filteredTagsNames;
    private Integer filteredPriority;
}
