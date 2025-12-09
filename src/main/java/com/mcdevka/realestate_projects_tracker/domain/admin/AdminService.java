package com.mcdevka.realestate_projects_tracker.domain.admin;

import com.mcdevka.realestate_projects_tracker.domain.admin.dto.AssignCompanyRequest;
import com.mcdevka.realestate_projects_tracker.domain.admin.dto.GrantPermissionsRequest;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccess;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccessRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ProjectAccessRepository projectAccessRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public void assignUserToCompany(AssignCompanyRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("User not found!"));
        user.setCompany(request.company());
        userRepository.save(user);
    }

    @Transactional
    public void grantUserPermissions(GrantPermissionsRequest request,
                                     Long userId) {
        User user =  userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("User not found!"));

        Set<ProjectPermissions> grantedPermissions = request.grantedPermissions();

        Project project = projectRepository.findById(request.projectId())
                                            .orElseThrow(()-> new EntityNotFoundException("Project not found!"));

        if (user.getCompany() == null || !user.getCompany().equals(project.getCompanyResposible())) {
            throw new IllegalStateException("User: " + user.getId() + " does not belong to the company: "
            + project.getCompanyResposible() + " with project: " + project.getName());
        }

        ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(user.getId(), project.getId())
                .orElse(ProjectAccess.builder()
                        .user(user)
                        .project(project)
                        .build());

        access.setPermissions(request.grantedPermissions());;
        projectAccessRepository.save(access);
    }

    public void deleteUser(){}
}
