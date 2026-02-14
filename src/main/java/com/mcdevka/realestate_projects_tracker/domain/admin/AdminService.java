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

import java.util.*;
import java.util.stream.Collectors;

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
        // 1. Znajdź użytkownika
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("User not found!"));

        // 2. Pobierz listę ID z requestu (zabezpiecz przed nullem)
        List<Long> ids = request.getCompanyIds();
        if (ids == null) {
            ids = List.of();
        }

        List<Long> oldCompaniesIds = user.getCompanies().stream().map(Company::getId).toList();
        List<Long> exclusiveOldCompaniesIds = new ArrayList<>(oldCompaniesIds);
        exclusiveOldCompaniesIds.removeAll(ids);

        List<Long> exclusiveOldCompaniesProjectsIds = projectRepository.findProjectIdsByCompanyIds(exclusiveOldCompaniesIds);

        // 3. Znajdź wszystkie firmy pasujące do tych ID
        List<Company> companiesToAssign = companyRepository.findAllById(ids);

        // 4. WAŻNE: Nadpisz listę firm użytkownika
        // Używamy Set (HashSet), aby uniknąć duplikatów
        user.setCompanies(new HashSet<>(companiesToAssign));

        // 5. Zapisz użytkownika (Hibernate zaktualizuje tabelę user_companies)
        userRepository.save(user);

        // 6. OPCJONALNIE: Nadawanie uprawnień.
        // Jeśli ta linijka powoduje błędy, zakomentuj ją na chwilę, żeby sprawdzić czy samo przypisywanie działa.
        try {
            projectAccessService.assignDefaultPermissionsOnUserCreation(user);
            projectAccessRepository.deleteAllByUserIdAndProjectsIds(user.getId(), exclusiveOldCompaniesProjectsIds);
        } catch (Exception e) {
            System.err.println("Nie udało się nadać domyślnych uprawnień: " + e.getMessage());
        }
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