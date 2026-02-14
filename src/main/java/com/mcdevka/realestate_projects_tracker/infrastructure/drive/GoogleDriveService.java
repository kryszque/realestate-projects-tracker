package com.mcdevka.realestate_projects_tracker.infrastructure.drive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleDriveService {

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
        fileMetadata.setParents(Collections.singletonList(parentId));

        return driveService.files().create(fileMetadata)
                .setFields("id, webViewLink")
                .execute();
    }

    // Pobiera informacje o istniejącym folderze
    public File getFolder(String folderId) throws IOException {
        return driveService.files().get(folderId)
                .setFields("id, webViewLink")
                .execute();
    }

    // Wgrywa plik
    public File uploadFile(MultipartFile file, String parentFolderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());

        // Ważne: parentFolderId nie może być nullem tutaj,
        // ale zabezpieczyliśmy to w Controllerze.
        if (parentFolderId != null) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        InputStreamContent mediaContent = new InputStreamContent(file.getContentType(), file.getInputStream());

        return driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink, name")
                .execute();
    }
}