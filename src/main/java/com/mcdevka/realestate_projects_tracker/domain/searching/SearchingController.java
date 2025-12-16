package com.mcdevka.realestate_projects_tracker.domain.searching;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:5173")
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
