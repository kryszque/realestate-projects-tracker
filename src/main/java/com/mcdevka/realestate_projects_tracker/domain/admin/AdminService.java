package com.mcdevka.realestate_projects_tracker.domain.admin;

import com.mcdevka.realestate_projects_tracker.domain.admin.dto.AssignCompanyRequest;
import com.mcdevka.realestate_projects_tracker.domain.admin.dto.GrantPermissionsRequest;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccess;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccessRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccessService;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ProjectAccessRepository projectAccessRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAccessService projectAccessService;

    @Value("${application.default-admin.mail}")
    private String adminMail;

    @Transactional
    public void assignUserToCompany(AssignCompanyRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("User not found!"));
        String oldCompany = user.getCompany();
        user.setCompany(request.company());
        projectAccessService.assignDefaultPermissionsOnUserCreation(user);
        projectAccessService.deleteOldPermissions(userId, oldCompany);
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

        access.setPermissions(grantedPermissions);
        projectAccessRepository.save(access);
    }

    @Transactional
    public User deleteUser(Long userId){
        User choseUser = userRepository.findById(userId).orElseThrow(
                ()-> new EntityNotFoundException("User not found!"));

        userRepository.delete(choseUser);

        return choseUser;
    }

    @Transactional
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @Transactional
    public void grantSystemAdminAccess(Project project) {
        userRepository.findByEmail(adminMail).ifPresent(admin -> {

            ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(admin.getId(), project.getId())
                    .orElse(ProjectAccess.builder()
                            .user(admin)
                            .project(project)
                            .permissions(new HashSet<>())
                            .build());

            Set<ProjectPermissions> newPermissions = new HashSet<>(access.getPermissions());
            newPermissions.add(ProjectPermissions.ADMIN);

            access.setPermissions(newPermissions);

            projectAccessRepository.save(access);
        });
    }
}
