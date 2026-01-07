package com.mcdevka.realestate_projects_tracker.domain.user.dto;

import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;

import java.util.Set;

public record UserProject(Long projectId, String projectName, Set<ProjectPermissions> permissions) { }
