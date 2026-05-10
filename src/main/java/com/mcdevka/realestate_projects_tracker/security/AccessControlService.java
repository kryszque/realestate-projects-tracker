package com.mcdevka.realestate_projects_tracker.security;

import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccess;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccessRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.user.Role;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final ProjectRepository projectRepository;
    private final ProjectAccessRepository projectAccessRepository;

    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public void checkAccess(Long projectId, ProjectPermissions requiredPermission){
        User currentUser = getCurrentUser();

        if(currentUser.getRole() == Role.ADMIN) {
            return;
        }

        if(currentUser.getCompanies() == null || currentUser.getCompanies().isEmpty()){
            throw new SecurityException("Nie jesteś przypisany do żadnej firmy.");
        }

        // Zwróć uwagę, że tu rzucamy IllegalArgumentException, żeby poleciał status 400 zamiast 403 (brak dostępu)
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Projekt o podanym ID nie istnieje."));

        if(!currentUser.getCompanies().contains(project.getCompany())){
            throw new SecurityException("Ten projekt nie jest przypisany do żadnej z Twoich firm.");
        }

        ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(currentUser.getId(), projectId)
                .orElseThrow(() -> new SecurityException("Nie masz przypisanego dostępu do tego projektu."));

        if(!access.getPermissions().contains(requiredPermission)){
            throw new SecurityException("Brak uprawnień. Do wykonania tej akcji wymagane jest uprawnienie: " + requiredPermission.name());
        }
    }

    public void checkAccessWithoutProjectId(ProjectPermissions requiredPermission){
        User currentUser = getCurrentUser();
        if(currentUser.getRole() == Role.ADMIN) {
            return;
        }
        throw new SecurityException("Brak uprawnień do wykonania tej operacji.");
    }
}