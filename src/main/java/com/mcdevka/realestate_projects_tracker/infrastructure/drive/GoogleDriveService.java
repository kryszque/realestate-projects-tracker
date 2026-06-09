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
import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.api.client.http.FileContent;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Arrays;

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
                .setSupportsAllDrives(true)
                .execute()
                .getPermissions();

        for (Permission p : permissions) {
            if (p.getEmailAddress() != null && p.getEmailAddress().equalsIgnoreCase(userEmail)) {
                try {
                    driveService.permissions().delete(folderId, p.getId())
                            .setSupportsAllDrives(true)
                            .execute();
                } catch (Exception e) {
                    System.err.println("Nie udało się usunąć uprawnienia z Dysku: " + e.getMessage());
                }
                break;
            }
        }
    }

    public void assignUserToDriveFiles(Long projectId, Long userId, String userRole, Set<ProjectPermissions> projectPermissions) {
        try {
            Project project = projectRepository.findById(projectId).orElseThrow();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Użytkownik o ID " + userId + " nie istnieje"));

            boolean hasWriterAccess = false;

            if (projectPermissions != null && !projectPermissions.isEmpty()) {
                hasWriterAccess = projectPermissions.contains(ProjectPermissions.ADMIN) ||
                        projectPermissions.contains(ProjectPermissions.CAN_EDIT) ||
                        projectPermissions.contains(ProjectPermissions.CAN_CREATE) ||
                        projectPermissions.contains(ProjectPermissions.CAN_DELETE);
            } else if (userRole != null) {
                Set<String> writerRoles = Set.of("ADMIN", "CAN_EDIT", "CAN_DELETE", "CAN_CREATE");
                hasWriterAccess = writerRoles.contains(userRole);
            }

            String driveRole = hasWriterAccess ? "writer" : "reader";

            if (project.getDriveFolderId() != null) {
                shareFolder(project.getDriveFolderId(), user.getGoogleDriveEmail(), driveRole);
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
                unshareFolder(project.getDriveFolderId(), user.getGoogleDriveEmail());
            }
        } catch (Exception e) {
            System.err.println("Błąd usuwania dostępu z Google Drive: " + e.getMessage());
        }
    }

    public void assignUserToCompanyFolder(Company company, String userEmail, String role) {
        if (company.getDriveFolderId() != null) {
            try {
                shareFolder(company.getDriveFolderId(), userEmail, role);
            } catch (Exception e) {
                System.err.println("Błąd nadawania dostępu do folderu firmy: " + e.getMessage());
            }
        }
    }

    public void removeUserFromCompanyFolder(Company company, String userEmail) {
        if (company.getDriveFolderId() != null) {
            try {
                unshareFolder(company.getDriveFolderId(), userEmail);
            } catch (Exception e) {
                System.err.println("Błąd usuwania dostępu z folderu firmy: " + e.getMessage());
            }
        }
    }

    public void changeFolderName(String folderId, String newFolderName) {
        if (folderId == null || folderId.isBlank()) return;

        try {
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(newFolderName);

            driveService.files().update(folderId, fileMetadata)
                    .setSupportsAllDrives(true)
                    .execute();

            System.out.println("Zmieniono nazwę folderu na Dysku o ID " + folderId + " na: " + newFolderName);
        } catch (Exception e) {
            System.err.println("Błąd podczas zmiany nazwy folderu na Dysku: " + e.getMessage());
        }
    }


    public void moveFolder(String fileId, String oldParentId, String newParentId) {
        if (fileId == null || oldParentId == null || newParentId == null) return;

        try {
            driveService.files().update(fileId, null)
                    .setAddParents(newParentId)
                    .setRemoveParents(oldParentId)
                    .setFields("id, parents")
                    .setSupportsAllDrives(true)
                    .execute();

            System.out.println("Przeniesiono folder " + fileId + " z " + oldParentId + " do " + newParentId);
        } catch (IOException e) {
            System.err.println("Błąd podczas przenoszenia folderu na Dysku Google: " + e.getMessage());
        }
    }
    // Pobiera folder "Backups" lub go tworzy, jeśli nie istnieje na poziomie rootFolderId
    public File getOrCreateFolder(String folderName, String parentId) throws IOException {
        String query = String.format("mimeType='application/vnd.google-apps.folder' and name='%s' and '%s' in parents and trashed=false", folderName, parentId);
        var fileList = driveService.files().list()
        .setQ(query)
        .setFields("files(id, name)")
        .setSupportsAllDrives(true)
        .setIncludeItemsFromAllDrives(true)
        .execute().getFiles();
        
        if (!fileList.isEmpty()) {
            return fileList.get(0);
        }
        return createFolder(folderName, parentId);
    }

    // Wgrywa lokalny plik (.sql.gz) stworzony przez skrypt
    public File uploadLocalFile(java.io.File file, String parentFolderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(file.getName());
        fileMetadata.setParents(Collections.singletonList(parentFolderId));
        
        FileContent mediaContent = new FileContent("application/gzip", file);
        return driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name")
                .setSupportsAllDrives(true)
                .execute();
    }

    // Listuje pliki w folderze (aby na froncie móc wybrać plik do przywrócenia)
    public List<File> listFilesInFolder(String folderId) throws IOException {
        String query = String.format("'%s' in parents and trashed=false", folderId);
        return driveService.files().list()
                .setQ(query)
                .setFields("files(id, name, createdTime)")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .execute()
                .getFiles();
    }

    // Pobiera plik z Dysku Google na serwer
    public void downloadFile(String fileId, java.io.File destFile) throws IOException {
        try (OutputStream out = new FileOutputStream(destFile)) {
            driveService.files().get(fileId).executeMediaAndDownloadTo(out);
        }
    }
}