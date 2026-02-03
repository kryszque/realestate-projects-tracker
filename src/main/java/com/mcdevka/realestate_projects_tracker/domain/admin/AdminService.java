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
    private final CompanyRepository companyRepository; // <--- Nowe wstrzyknięcie
    private final ProjectAccessService projectAccessService;

    @Value("${application.default-admin.mail}")
    private String adminMail;

    // Metoda teraz DODAJE użytkownika do firmy (nie usuwając poprzednich)
    @Transactional
    public void assignUserToCompany(AssignCompanyRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("User not found!"));

        // Zakładam, że w request przesyłasz ID firmy (Long companyId)
        // Jeśli przesyłasz nazwę, użyj findByName
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found!"));

        // Relacja ManyToMany - dodajemy firmę do zbioru
        user.getCompanies().add(company);

        // Zapisujemy usera (zaktualizuje to tabelę łączącą user_companies)
        userRepository.save(user);

        // Nadajemy uprawnienia do projektów tej NOWEJ firmy
        // (Metoda w ProjectAccessService musi być dostosowana, by obsłużyć tę sytuację)
        projectAccessService.assignDefaultPermissionsOnUserCreation(user);
    }

    @Transactional
    public void grantUserPermissions(GrantPermissionsRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("User not found!"));

        Set<ProjectPermissions> grantedPermissions = request.grantedPermissions();

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(()-> new EntityNotFoundException("Project not found!"));

        // ZMIANA: Sprawdzamy czy zbiór firm użytkownika zawiera firmę projektu
        if (user.getCompanies() == null || !user.getCompanies().contains(project.getCompany())) {
            throw new IllegalStateException("User: " + user.getId() + " does not belong to the company responsible for project: " + project.getName());
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