package com.mcdevka.realestate_projects_tracker.domain.user.dto;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;

import java.util.List;
import java.util.Set;

public record UserDetail(Long id, String email,
                         String firstName,
                         String lastName,
                         String role,
                         Set<Company> companies,
                         List<UserProject> assignedProjects) { }
