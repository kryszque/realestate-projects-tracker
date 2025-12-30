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
        if(currentUser.getCompany() == null){
            throw new SecurityException("You are not assigned to any company!");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new SecurityException("Project with id " + projectId + " does not exist!"));

        if(!currentUser.getCompany().equals(project.getCompanyResposible())){
            throw new SecurityException("This project isn't assigned to your company!");
        }

        ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(currentUser.getId(),
                projectId).orElseThrow(() -> new SecurityException("You don't have access to do this project!"));

        if(!access.getPermissions().contains(requiredPermission)){
            throw new SecurityException("You don't have permission to do this action! You need " + requiredPermission);
        }

    }

    public void checkAccessWithoutProjectId(ProjectPermissions requiredPermission){
        User currentUser = getCurrentUser();
        if(currentUser.getRole() == Role.ADMIN) { }
        else{
            throw new SecurityException("You don't have permission to perform this action!");
        }
    }
}
