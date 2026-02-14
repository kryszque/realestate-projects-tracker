package com.mcdevka.realestate_projects_tracker.domain.searching;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import com.mcdevka.realestate_projects_tracker.domain.item.ItemService;
import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarService;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectService;
import com.mcdevka.realestate_projects_tracker.domain.user.Role;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalSearchingService {

    private final ProjectService projectService;
    private final PillarService pillarService;
    private final ItemService itemService;
    private final FilteringService filteringService;
    private final UserService userService; // Wstrzykujemy

    public GlobalSearchingResultDTO searchInDatabase(SearchingCriteria criteria) {

        // 👇 1. ZABEZPIECZENIE: Wstrzyknij firmy użytkownika do kryteriów
        injectUserSecurityContext(criteria);

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

    private void injectUserSecurityContext(SearchingCriteria criteria) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return;

        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        // Jeśli to ADMIN, nie ustawiamy ograniczeń (null oznacza "pokaż wszystko")
        if (currentUser.getRole() == Role.ADMIN) {
            criteria.setUserAllowedCompanyIds(null);
            return;
        }

        // Jeśli to USER, pobieramy ID firm przypisanych do niego
        // Zakładam, że User ma relację np. user.getCompanies() typu Set<Company>
        List<Long> allowedIds = currentUser.getCompanies().stream()
                .map(Company::getId)
                .collect(Collectors.toList());

        // Jeśli user nie ma żadnej firmy, dajemy listę z niemożliwym ID (-1), żeby nic nie zwróciło
        if (allowedIds.isEmpty()) {
            allowedIds = List.of(-1L);
        }

        criteria.setUserAllowedCompanyIds(allowedIds);
    }
}