package com.mcdevka.realestate_projects_tracker.domain.searching;


import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import com.mcdevka.realestate_projects_tracker.domain.item.ItemService;
import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarService;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchingController<T>{

    private final ProjectService projectService;
    private final PillarService pillarService;
    private final ItemService itemService;

    public  SearchingController(ProjectService projectService,  PillarService pillarService,
                                ItemService itemService) {
        this.projectService = projectService;
        this.pillarService = pillarService;
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<GlobalSearchingResultDTO> search(
            @ModelAttribute SearchingCriteria criteria){

        var searchedProjects = projectService.searchProjects(criteria);
        var searchedPillars = pillarService.searchPillars(criteria);
        var searchedItems = itemService.searchItems(criteria);

        GlobalSearchingResultDTO result = new GlobalSearchingResultDTO(
                searchedProjects, searchedPillars, searchedItems);

        return ResponseEntity.ok(result);
    }
}
