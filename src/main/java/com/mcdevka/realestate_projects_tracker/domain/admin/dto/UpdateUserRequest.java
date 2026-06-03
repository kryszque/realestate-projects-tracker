package com.mcdevka.realestate_projects_tracker.domain.admin.dto;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String email,
        String googleDriveEmail,
        boolean canCreateProjects,
        boolean canDeleteProjects
) {}
