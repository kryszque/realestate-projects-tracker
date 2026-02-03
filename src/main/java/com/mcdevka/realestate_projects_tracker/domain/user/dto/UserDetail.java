package com.mcdevka.realestate_projects_tracker.domain.user.dto;

import java.util.List;

public record UserDetail(Long id, String email,
                         String firstName,
                         String lastName,
                         String role,
                         List<String> companies,
                         List<UserProject> assignedProjects) { }
