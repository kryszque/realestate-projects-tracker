package com.mcdevka.realestate_projects_tracker.domain.company;

import com.mcdevka.realestate_projects_tracker.domain.user.Role;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.infrastructure.drive.GoogleDriveService;
import com.mcdevka.realestate_projects_tracker.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AccessControlService accessControlService;
    private final GoogleDriveService googleDriveService;

    public List<Company> getAllCompanies() {return companyRepository.findAll();}

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company with ID " + id + " not found!"));
    }
    public Company createCompany(Company inputCompany) {
        User currentUser = accessControlService.getCurrentUser();
        if(currentUser.getRole() !=  Role.ADMIN) {
            throw new SecurityException("Only admins can perform this action!");
        }
        if (companyRepository.existsByName(inputCompany.getName())) {
            throw new IllegalArgumentException("Company with name " + inputCompany.getName() + " already exists!");
        }

        Company createdCompany = new Company();
        createdCompany.setName(inputCompany.getName());

        try {
            com.google.api.services.drive.model.File companyFolder =
                    googleDriveService.createFolder(createdCompany.getName(), null);
            createdCompany.setDriveFolderId(companyFolder.getId());
            createdCompany.setDriveFolderLink(companyFolder.getWebViewLink());
        } catch (Exception e) {
            System.err.println("Nie udało się utworzyć folderu na Dysku Google dla firmy: " + e.getMessage());
        }

        return companyRepository.save(createdCompany);
    }

    public Company updateCompany(Long id, Company inputCompany) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        String oldName = company.getName();
        String newName = inputCompany.getName();

        company.setName(inputCompany.getName());

        if (!oldName.equals(newName) && company.getDriveFolderId() != null) {
            googleDriveService.changeFolderName(company.getDriveFolderId(), newName);
        }

        return companyRepository.save(company);
    }

    public Company archiveCompany(Long id){
        User currentUser = accessControlService.getCurrentUser();
        if(currentUser.getRole() !=  Role.ADMIN) {
            throw new SecurityException("Only admins can perform this action!");
        }
        Company archivedCompany = getCompanyById(id);
        archivedCompany.setState("archived");
        return companyRepository.save(archivedCompany);
    }
}
