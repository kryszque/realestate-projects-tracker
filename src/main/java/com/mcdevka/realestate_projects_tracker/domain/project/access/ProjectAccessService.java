package com.mcdevka.realestate_projects_tracker.domain.project.access;

import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserRepository;
import com.mcdevka.realestate_projects_tracker.domain.user.dto.UserProject;
import jakarta.persistence.EntityNotFoundException;
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

    public void assignDefaultPermissionOnProjectCreation(Project project){
        String companyResponsible = project.getCompanyResposible();
        List<User> users = userRepository.findByCompany(companyResponsible);
        for(User user : users){
            ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(user.getId(), project.getId())
                    .orElse(ProjectAccess.builder()
                            .user(user)
                            .project(project)
                            .build());

            access.setPermissions(new HashSet<>(Set.of(ProjectPermissions.CAN_VIEW)));
            projectAccessRepository.save(access);
        }
    }

    public void assignDefaultPermissionsOnUserCreation(User user){
        String company = user.getCompany();
        List<Project> projects = projectRepository.findByStateNotAndCompanyResposible("archived", company);
        for(Project project : projects){
            ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(user.getId(), project.getId())
                    .orElse(ProjectAccess.builder()
                            .user(user)
                            .project(project)
                            .build());

            access.setPermissions(new HashSet<>(Set.of(ProjectPermissions.CAN_VIEW)));
            projectAccessRepository.save(access);
        }
    }

    public void deleteOldPermissions(Long userId, String oldCompany){
        List<Project> projects = projectRepository.findByStateNotAndCompanyResposible("archived", oldCompany);
        for(Project project : projects){
            projectAccessRepository.deleteByUserIdAndProjectId(userId, project.getId());
        }

    }
}
