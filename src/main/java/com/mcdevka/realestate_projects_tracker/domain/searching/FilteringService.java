package com.mcdevka.realestate_projects_tracker.domain.searching;

import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilteringService {

    public GlobalSearchingResultDTO filterSearch(GlobalSearchingResultDTO sourceData, FilteringCriteria criteria) {

        List<Project> projects = criteria.isFilterByProject() ? sourceData.projects() : Collections.emptyList();
        List<Pillar> pillars = criteria.isFilterByPillar() ? sourceData.pillars() : Collections.emptyList();
        List<Item> items = criteria.isFilterByItem() ? sourceData.items() : Collections.emptyList();

        if (!CollectionUtils.isEmpty(criteria.getFilteredTagsNames())) {
            projects = filterProjects(projects, criteria.getFilteredTagsNames(), criteria.getFilteredPriority());
            pillars = filterPillars(pillars, criteria.getFilteredTagsNames(),  criteria.getFilteredPriority());
            items = filterItems(items, criteria.getFilteredTagsNames(), criteria.getFilteredPriority());
        }

        return new GlobalSearchingResultDTO(projects, pillars, items);
    }

    private List<Item> filterItems(List<Item> items, List<String> tagNamesToFind, Integer filteredPriority) {
        if (CollectionUtils.isEmpty(items)) return items;
        return items.stream()
                .filter(item -> containsAllTags(item.getTags(), tagNamesToFind))
                .filter(item ->matchesPriority(item.getPriority(), filteredPriority))
                .collect(Collectors.toList());
    }

    private List<Project> filterProjects(List<Project> projects, List<String> tagNamesToFind,
                                         Integer filteredPriority) {
        if (CollectionUtils.isEmpty(projects)) return projects;
        return projects.stream()
                .filter(project -> containsAllTags(project.getTags(), tagNamesToFind))
                .filter(project -> matchesPriority(project.getPriority(),filteredPriority))
                .collect(Collectors.toList());
    }

    private List<Pillar> filterPillars(List<Pillar> pillars, List<String> tagNamesToFind,
                                       Integer filteredPriority) {
        if (CollectionUtils.isEmpty(pillars)) return pillars;
        return pillars.stream()
                .filter(pillar -> containsAllTags(pillar.getTags(), tagNamesToFind))
                .filter(pillar -> matchesPriority(pillar.getPriority(),filteredPriority))
                .collect(Collectors.toList());
    }

    private boolean containsAllTags(Set<Tag> objectTags, List<String> searchingTagNames) {
        if (CollectionUtils.isEmpty(searchingTagNames)) {
            return true;
        }

        if (CollectionUtils.isEmpty(objectTags)) {
            return false;
        }

        Set<String> objectTagNames = objectTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        return objectTagNames.containsAll(searchingTagNames);
    }

    private boolean matchesPriority(Integer itemPriority, Integer filteredPriority) {
        if (filteredPriority == null) {
            return true;
        }

        return itemPriority != null && itemPriority.equals(filteredPriority);
    }
}