package com.mcdevka.realestate_projects_tracker.domain.admin;

import com.mcdevka.realestate_projects_tracker.domain.admin.dto.AssignCompanyRequest;
import com.mcdevka.realestate_projects_tracker.domain.admin.dto.GrantPermissionsRequest;
import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.company.CompanyRepository;
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
    private final CompanyRepository companyRepository;

    @Value("${application.default-admin.mail}")
    private String adminMail;

    @Transactional
    public void addCompanyToUser(AssignCompanyRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("User not found!"));
        Company company = companyRepository.findById(request.company().getId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found!"));
        user.getCompanies().add(company);
        projectAccessService.assignDefaultPermissionsOnUserCreation(user);
        userRepository.save(user);
    }

    @Transactional
    public void deleteCompanyFromUser(AssignCompanyRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("User not found!"));
        Company companyToRemove = user.getCompanies().stream()
                .filter(c -> c.getId().equals(request.company().getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User does not belong to this company!"));
        user.getCompanies().remove(companyToRemove);
        projectAccessService.deleteOldPermissions(userId, companyToRemove);
        userRepository.save(user);
    }

    @Transactional
    public void grantUserPermissions(GrantPermissionsRequest request,
                                     Long userId) {
        User user =  userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("User not found!"));

        Set<ProjectPermissions> grantedPermissions = request.grantedPermissions();

        Project project = projectRepository.findById(request.projectId())
                                            .orElseThrow(()-> new EntityNotFoundException("Project not found!"));

        if (user.getCompanies() == null || !user.getCompanies().contains(project.getCompanyResposible())) {
            throw new IllegalStateException("User: " + user.getId() + " does not belong to the company: "
            + project.getCompanyResposible() + " with project: " + project.getName());
        }

        ProjectAccess access = projectAccessRepository.findByUserIdAndProjectId(user.getId(), project.getId())
                .orElse(ProjectAccess.builder()
                        .user(user)
                        .project(project)
                        .build());

        access.setPermissions(new HashSet<>(grantedPermissions));
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
