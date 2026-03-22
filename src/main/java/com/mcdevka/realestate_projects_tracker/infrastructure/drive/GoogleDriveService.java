package com.mcdevka.realestate_projects_tracker.infrastructure.drive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.drive.model.Permission;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GoogleDriveService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository; // 🔴 POPRAWKA: Dodano słowo 'final'!

    @Value("${google.drive.credentials.file.path}")
    private String credentialsFilePath;

    @Value("${google.drive.root.folder.id}")
    private String rootFolderId;

    private Drive driveService;

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(credentialsFilePath.replace("classpath:", ""));
        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singletonList(DriveScopes.DRIVE));

        driveService = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("RealEstate Tracker")
                .build();
    }

    public String getRootFolderId() {
        return rootFolderId;
    }

    // Tworzy nowy folder i zwraca jego ID
    public File createFolder(String folderName, String parentId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        // 🔴 POPRAWKA: Sprawdzamy czy parentId istnieje.
        // Jeśli nie, wrzucamy do głównego folderu konfiguracyjnego (rootFolderId)
        if (parentId != null && !parentId.isBlank()) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        } else if (rootFolderId != null && !rootFolderId.isBlank()) {
            fileMetadata.setParents(Collections.singletonList(rootFolderId));
        }

        return driveService.files().create(fileMetadata)
                .setFields("id, webViewLink")
                .setSupportsAllDrives(true)
                .execute();
    }

    // Pobiera informacje o istniejącym folderze
    public File getFolder(String folderId) throws IOException {
        return driveService.files().get(folderId)
                .setFields("id, webViewLink")
                .setSupportsAllDrives(true)
                .execute();
    }

    // Wgrywa plik
    public File uploadFile(MultipartFile file, String parentFolderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());

        if (parentFolderId != null && !parentFolderId.isBlank()) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        InputStreamContent mediaContent = new InputStreamContent(file.getContentType(), file.getInputStream());

        return driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink, name")
                .setSupportsAllDrives(true)
                .execute();
    }

    public void shareFolder(String folderId, String userEmail, String role) throws IOException {
        // Rola: 'reader' (czytanie), 'writer' (edycja), 'commenter' (komentowanie)
        Permission userPermission = new Permission()
                .setType("user")
                .setRole(role)
                .setEmailAddress(userEmail);

        try {
            driveService.permissions().create(folderId, userPermission)
                    .setFields("id")
                    .setSendNotificationEmail(false)
                    .setSupportsAllDrives(true)
                    .execute();
        } catch (Exception e) {
            System.err.println("Nie udało się udostępnić folderu dla: " + userEmail + ". Błąd: " + e.getMessage());
        }
    }

    public void unshareFolder(String folderId, String userEmail) throws IOException {
        var permissions = driveService.permissions().list(folderId)
                .setFields("permissions(id, emailAddress)")
                .execute()
                .getPermissions();

        for (Permission p : permissions) {
            if (userEmail.equalsIgnoreCase(p.getEmailAddress())) {
                driveService.permissions().delete(folderId, p.getId())
                        .setSupportsAllDrives(true)
                        .execute();
                break;
            }
        }
    }

    public void assignUserToDriveFiles(Long projectId, Long userId, String userRole, Set<ProjectPermissions> projectPermissions) {
        try {
            Project project = projectRepository.findById(projectId).orElseThrow();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Użytkownik o ID " + userId + " nie istnieje"));

            Set<String> writerRoles = Set.of("ADMIN", "CAN_EDIT", "CAN_DELETE", "CAN_CREATE");
            String driveRole;

            if(projectPermissions != null && !projectPermissions.isEmpty()) {
                driveRole = (!Collections.disjoint(writerRoles, projectPermissions)) ? "writer" : "reader";
            }
            else{
                driveRole = (writerRoles.contains(userRole)) ? "writer" : "reader";
            }

            if (project.getDriveFolderId() != null) {
                shareFolder(project.getDriveFolderId(), user.getEmail(), driveRole);
            }
        } catch (Exception e) {
            System.err.println("Błąd synchronizacji z Google Drive: " + e.getMessage());
        }
    }

    public void removeUserFromDriveFiles(Long projectId, Long userId) {
        try {
            Project project = projectRepository.findById(projectId).orElseThrow();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Użytkownik o ID " + userId + " nie istnieje"));

            if (project.getDriveFolderId() != null) {
                unshareFolder(project.getDriveFolderId(), user.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Błąd usuwania dostępu z Google Drive: " + e.getMessage());
        }
    }
}