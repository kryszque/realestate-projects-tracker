package com.mcdevka.realestate_projects_tracker.domain.searching;

import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import com.mcdevka.realestate_projects_tracker.domain.item.ItemService;
import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarService;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalSearchingService {

    private final ProjectService projectService;
    private final PillarService pillarService;
    private final ItemService itemService;
    private final FilteringService filteringService;

    public GlobalSearchingResultDTO searchInDatabase(SearchingCriteria criteria) {
        List<Project> searchedProjects = Collections.emptyList();
        if (criteria.getPillarId() == null) {
            searchedProjects = projectService.searchProjects(criteria);
        }

        List<Pillar> searchedPillars = Collections.emptyList();
        if (criteria.getPillarId() == null) {
            searchedPillars = pillarService.searchPillars(criteria);
        }

        List<Item> searchedItems = itemService.searchItems(criteria);

        return new GlobalSearchingResultDTO(searchedProjects, searchedPillars, searchedItems);
    }

    public GlobalSearchingResultDTO searchAndFilter(SearchingCriteria searchCriteria, FilteringCriteria filterCriteria) {
        GlobalSearchingResultDTO rawResults = searchInDatabase(searchCriteria);

        return filteringService.filterSearch(rawResults, filterCriteria);
    }
}