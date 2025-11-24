package com.mcdevka.realestate_projects_tracker.domain.searching;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchingController{

    private final GlobalSearchingService globalSearchingService;

    @GetMapping
    public ResponseEntity<GlobalSearchingResultDTO> search(
            @ModelAttribute SearchingCriteria searchingCriteria){

        GlobalSearchingResultDTO searchResult = globalSearchingService.searchInDatabase(searchingCriteria);

        return ResponseEntity.ok(searchResult);
    }

    @GetMapping("/filter")
    public ResponseEntity<GlobalSearchingResultDTO> filterSearch(
            @ModelAttribute SearchingCriteria searchingCriteria,
            @ModelAttribute FilteringCriteria filteringCriteria
    ){
        GlobalSearchingResultDTO filterResult = globalSearchingService.
                                                searchAndFilter(searchingCriteria, filteringCriteria);

        return ResponseEntity.ok(filterResult);
    }
}
