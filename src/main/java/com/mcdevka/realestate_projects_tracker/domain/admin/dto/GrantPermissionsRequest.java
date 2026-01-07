package com.mcdevka.realestate_projects_tracker.domain.admin.dto;


import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;

import java.util.Set;

public record GrantPermissionsRequest(Set<ProjectPermissions> grantedPermissions,
                                      Long projectId) {}
