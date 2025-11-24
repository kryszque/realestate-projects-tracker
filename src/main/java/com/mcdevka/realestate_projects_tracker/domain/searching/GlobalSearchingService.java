package com.mcdevka.realestate_projects_tracker.domain.searching;

import com.mcdevka.realestate_projects_tracker.domain.item.ItemService;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarService;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GlobalSearchingService {

    private final ProjectService projectService;
    private final PillarService pillarService;
    private final ItemService itemService;
    private final FilteringService filteringService;

    public GlobalSearchingResultDTO searchInDatabase(SearchingCriteria criteria) {
        var searchedProjects = projectService.searchProjects(criteria);
        var searchedPillars = pillarService.searchPillars(criteria);
        var searchedItems = itemService.searchItems(criteria);

        return new GlobalSearchingResultDTO(searchedProjects, searchedPillars, searchedItems);
    }

    public GlobalSearchingResultDTO searchAndFilter(SearchingCriteria searchCriteria, FilteringCriteria filterCriteria) {
        GlobalSearchingResultDTO rawResults = searchInDatabase(searchCriteria);

        return filteringService.filterSearch(rawResults, filterCriteria);
    }
}