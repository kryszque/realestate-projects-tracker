package com.mcdevka.realestate_projects_tracker.domain.searching;

import com.mcdevka.realestate_projects_tracker.domain.item.ItemService;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarService;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectService;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GlobalSearchingService {

    private final ProjectService projectService;
    private final PillarService pillarService;
    private final ItemService itemService;
    private final FilteringService filteringService;

    public GlobalSearchingResultDTO searchInDatabase(SearchingCriteria criteria) {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var searchedProjects = projectService.searchProjects(criteria, currentUser);
        var searchedPillars = pillarService.searchPillars(criteria, currentUser);
        var searchedItems = itemService.searchItems(criteria, currentUser);

        return new GlobalSearchingResultDTO(searchedProjects, searchedPillars, searchedItems);
    }

    public GlobalSearchingResultDTO searchAndFilter(SearchingCriteria searchCriteria, FilteringCriteria filterCriteria) {
        GlobalSearchingResultDTO rawResults = searchInDatabase(searchCriteria);

        return filteringService.filterSearch(rawResults, filterCriteria);
    }
}