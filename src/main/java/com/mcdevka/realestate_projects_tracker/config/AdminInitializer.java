package com.mcdevka.realestate_projects_tracker.config;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.company.CompanyRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccess;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccessRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.user.Role;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final ProjectAccessRepository projectAccessRepository;
    private final CompanyRepository companyRepository; // <--- Nowe wstrzyknięcie

    @Value("${application.default-admin.firstName}")
    private String adminFirstName;
    @Value("${application.default-admin.lastName}")
    private String adminLastName;
    @Value("${application.default-admin.mail}")
    private String adminMail;
    @Value("${application.default-admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {

        // 1. Upewnij się, że firma SYSTEM istnieje
        Company systemCompany = companyRepository.findByName("SYSTEM")
                .orElseGet(() -> {
                    Company newCompany = new Company();
                    newCompany.setName("SYSTEM");
                    return companyRepository.save(newCompany);
                });

        if(userRepository.findByEmail(adminMail).isEmpty()) {
            String encodedPassword = passwordEncoder.encode(adminPassword);

            User admin = User.builder()
                    .firstname(adminFirstName)
                    .lastname(adminLastName)
                    .email(adminMail)
                    .password(encodedPassword)
                    .role(Role.ADMIN)
                    .companies(new HashSet<>(Set.of(systemCompany))) // <--- ZMIANA: Set firm
                    .build();

            userRepository.save(admin);
            grantAdminPermissions();
            System.out.println("Admin has been created");
        } else {
            System.out.println("Admin has already been created");
            grantAdminPermissions();
        }
    }

    private void grantAdminPermissions(){
        User savedAdmin =  userRepository.findByEmail(adminMail).orElseThrow();
        List<Project> allProjects = projectRepository.findAll();

        for(Project project : allProjects) {
            ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(savedAdmin.getId(),
                            project.getId())
                    .orElse(ProjectAccess.builder()
                            .user(savedAdmin)
                            .project(project)
                            .build());

            access.setPermissions(Set.of(ProjectPermissions.ADMIN));
            projectAccessRepository.save(access);
        }
    }
}