package com.mcdevka.realestate_projects_tracker.domain.searching;

import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;

import java.util.List;

public record GlobalSearchingResultDTO(List<Project> projects,
                                       List<Pillar> pillars,
                                       List<Item> items) {}
