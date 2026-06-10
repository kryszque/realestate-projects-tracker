package com.mcdevka.realestate_projects_tracker.domain.admin;

import com.mcdevka.realestate_projects_tracker.domain.admin.dto.AssignCompanyRequest;
import com.mcdevka.realestate_projects_tracker.domain.admin.dto.GrantPermissionsRequest;
import com.mcdevka.realestate_projects_tracker.domain.admin.dto.UpdateUserRequest;
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
import com.mcdevka.realestate_projects_tracker.infrastructure.drive.GoogleDriveService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProjectAccessRepository projectAccessRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final ProjectAccessService projectAccessService;
    private final GoogleDriveService googleDriveService;

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

        List<Long> exclusiveOldCompaniesProjectsIds = new ArrayList<>();
        if (!exclusiveOldCompaniesIds.isEmpty()) {
            exclusiveOldCompaniesProjectsIds = projectRepository.findProjectIdsByCompanyIds(exclusiveOldCompaniesIds);
        }
        // 3. Znajdź wszystkie firmy pasujące do tych ID
        List<Company> companiesToAssign = companyRepository.findAllById(ids);

        for (Long oldCompId : exclusiveOldCompaniesIds) {
            Company oldCompany = companyRepository.findById(oldCompId).orElse(null);
            if (oldCompany != null) {
                googleDriveService.removeUserFromCompanyFolder(oldCompany, user.getGoogleDriveEmail());
            }
        }

        if (exclusiveOldCompaniesProjectsIds != null && !exclusiveOldCompaniesProjectsIds.isEmpty()) {
            for (Long projId : exclusiveOldCompaniesProjectsIds) {
                googleDriveService.removeUserFromDriveFiles(projId, user.getId());
            }
        }

        for (Company newCompany : companiesToAssign) {
            if (!user.getCompanies().contains(newCompany)) {
                googleDriveService.assignUserToCompanyFolder(newCompany, user.getGoogleDriveEmail(), "reader");
            }
        }
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
        if(grantedPermissions.isEmpty()) {
            googleDriveService.removeUserFromDriveFiles(project.getId(), user.getId());
        }
        else{
            googleDriveService.assignUserToDriveFiles(project.getId(), user.getId(), null, grantedPermissions);
        }

        projectAccessRepository.save(access);
    }

    @Transactional
    public User deleteUser(Long userId){
        User choseUser = userRepository.findById(userId).orElseThrow(
                ()-> new EntityNotFoundException("User not found!"));
        googleDriveService.removeUserFromDriveFiles(choseUser.getId(), choseUser.getId());
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

    @Transactional
    public void updateUserDetails(long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFirstname(request.firstName());
        user.setLastname(request.lastName());
        user.setEmail(request.email());
        user.setGoogleDriveEmail(request.googleDriveEmail());
        user.setCanCreateProjects(request.canCreateProjects());
        user.setCanDeleteProjects(request.canDeleteProjects());

        userRepository.save(user);
    }

    // Automatycznie odpala backup co niedzielę o 2:00 w nocy
    @Scheduled(cron = "0 0 2 * * SUN") 
    @Transactional
    public void createAndUploadBackup() {
        try {
            // 1. Uruchomienie skryptu lokalnego
            ProcessBuilder pb = new ProcessBuilder("bash", "/app/scripts/backup.sh");
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Skrypt backupu zakończył się błędem!");
            }

            // 2. Odnalezienie nowostworzonego pliku w katalogu ./backups
            java.io.File backupDir = new java.io.File("/app/scripts/backups");
            java.io.File[] files = backupDir.listFiles((d, name) -> name.endsWith(".sql.gz"));
            if (files == null || files.length == 0) throw new RuntimeException("Nie znaleziono pliku backupu");

            // Sortujemy aby wybrać najnowszy plik
            java.io.File latestBackup = Arrays.stream(files)
                    .max(Comparator.comparingLong(java.io.File::lastModified))
                    .orElseThrow();

            // 3. Wrzucenie na Dysk Google obok folderów firm (w katalog główny aplikacji na Dysku)
            com.google.api.services.drive.model.File driveFolder = googleDriveService.getOrCreateFolder("Backups", googleDriveService.getRootFolderId());
            googleDriveService.uploadLocalFile(latestBackup, driveFolder.getId());
            
        } catch (Exception e) {
            System.err.println("Błąd procesu backupu: " + e.getMessage());
            throw new RuntimeException("Nie udało się utworzyć kopii zapasowej");
        }
    }

    @Transactional
    public void restoreFromBackup(String fileId) {
        try {
            java.io.File tempFile = new java.io.File("/app/scripts/backups/temp_restore.sql.gz");
            
            // 1. Pobierz wybrany plik backupu z GDrive
            googleDriveService.downloadFile(fileId, tempFile);

            // 2. Uruchomienie skryptu przywracania z podanym plikiem
            ProcessBuilder pb = new ProcessBuilder("bash", "/app/scripts/restore.sh", tempFile.getAbsolutePath());
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();

            // 3. Usunięcie pobranego pliku by nie zaśmiecać dysku
            if(tempFile.exists()) {
                tempFile.delete();
            }
        } catch (Exception e) {
            System.err.println("Błąd przywracania: " + e.getMessage());
            throw new RuntimeException("Nie udało się przywrócić bazy danych");
        }
    }

    public List<com.google.api.services.drive.model.File> getAvailableBackups() {
         try {
             com.google.api.services.drive.model.File driveFolder = googleDriveService.getOrCreateFolder("Backups", googleDriveService.getRootFolderId());
             return googleDriveService.listFilesInFolder(driveFolder.getId());
         } catch(Exception e) { 
             return List.of(); 
         }
    }
}