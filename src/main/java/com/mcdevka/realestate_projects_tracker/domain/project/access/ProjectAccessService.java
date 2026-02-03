package com.mcdevka.realestate_projects_tracker.domain.project.access;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserRepository;
import com.mcdevka.realestate_projects_tracker.domain.user.dto.UserProject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private final ProjectAccessRepository projectAccessRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public UserProject getUserProject(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("Project with id " + projectId + " not found"));

        ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(userId, projectId).orElse(null);

        if(access == null){
            return null;
        } else{
            return new UserProject(projectId, project.getName(), access.getPermissions());
        }
    }

    // Wywoływane, gdy tworzymy nowy projekt
    public void assignDefaultPermissionOnProjectCreation(Project project){
        Company company = project.getCompany();

        // ZMIANA: Szukamy userów, którzy mają tę firmę w swoim zbiorze firm
        List<User> users = userRepository.findAllByCompaniesContaining(company);

        for(User user : users){
            assignViewPermission(user, project);
        }
    }

    // Wywoływane, gdy tworzymy nowego użytkownika (lub dodajemy mu firmę)
    public void assignDefaultPermissionsOnUserCreation(User user){
        Set<Company> userCompanies = user.getCompanies();

        if (userCompanies == null || userCompanies.isEmpty()) {
            return;
        }

        // ZMIANA: Pobieramy projekty dla WSZYSTKICH firm użytkownika
        List<Project> projects = projectRepository.findByStateNotAndCompanyIn("archived", userCompanies);

        for(Project project : projects){
            assignViewPermission(user, project);
        }
    }

    // Pomocnicza metoda, żeby nie powielać kodu
    private void assignViewPermission(User user, Project project) {
        ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(user.getId(), project.getId())
                .orElse(ProjectAccess.builder()
                        .user(user)
                        .project(project)
                        .build());

        // Jeśli user nie ma jeszcze żadnych uprawnień, dajemy mu CAN_VIEW
        if (access.getPermissions() == null || access.getPermissions().isEmpty()) {
            access.setPermissions(new HashSet<>(Set.of(ProjectPermissions.CAN_VIEW)));
            projectAccessRepository.save(access);
        }
    }

    // Wywoływane, gdy usuwamy firmę z użytkownika
    @Transactional // Ważne przy usuwaniu
    public void deleteOldPermissions(Long userId, Company oldCompany){
        // Znajdź projekty należące do firmy, którą użytkownik opuścił
        // Tutaj szukamy konkretnie dla jednej firmy (dlatego 'AndCompany', a nie 'In')
        List<Project> projects = projectRepository.findByStateNotAndCompany("archived", oldCompany);

        for(Project project : projects){
            projectAccessRepository.deleteByUserIdAndProjectId(userId, project.getId());
        }
    }
}