package com.mcdevka.realestate_projects_tracker.domain.project;

import com.mcdevka.realestate_projects_tracker.domain.admin.AdminService;
import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.item.ItemRepository;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarRepository;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarService;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccessService;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import com.mcdevka.realestate_projects_tracker.domain.tag.TagRepository;
import com.mcdevka.realestate_projects_tracker.domain.user.Role;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.infrastructure.drive.GoogleDriveService;
import com.mcdevka.realestate_projects_tracker.security.AccessControlService;
import com.mcdevka.realestate_projects_tracker.security.annotation.CheckAccess;
import com.mcdevka.realestate_projects_tracker.security.annotation.ProjectId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PillarService pillarService;
    private final TagRepository tagRepository;
    private final AccessControlService accessControlService;
    private final ProjectAccessService projectAccessService;
    private final AdminService adminService;
    private final PillarRepository pillarRepository;
    private final ItemRepository itemRepository;
    private final GoogleDriveService googleDriveService;

    public List<Project> getAllProjects(){
        User currentUser = accessControlService.getCurrentUser();

        if(currentUser.getRole() == Role.ADMIN){
            return projectRepository.findByStateNot("archived");
        } else {
            Collection<Company> userCompanies = currentUser.getCompanies();

            if (userCompanies.isEmpty()) {
                return List.of();
            }

            return projectRepository.findByStateNotAndCompanyIn("archived", userCompanies);
        }
    }

    @CheckAccess(ProjectPermissions.CAN_VIEW)
    public Project getProjectById(@ProjectId Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + id + " not found!"));
    }

    @CheckAccess(ProjectPermissions.CAN_CREATE)
    public Project createProject(Project inputProject){

        checkForProjectDuplicates(inputProject);

        Project createdProject = new Project();

        setChangableFields(inputProject, createdProject);

        createdProject.setStartDate(LocalDate.now());
        createdProject.setState("active");
        createdProject.setPriority(inputProject.getPriority());
        createdProject.setPillars(pillarService.initializeDefaultPillars(createdProject));

        if (inputProject.getTags() != null && !inputProject.getTags().isEmpty()) {
            Set<Tag> tagsToAdd = new HashSet<>();

            for (var tagDto : inputProject.getTags()) {
                Tag tag = tagRepository.findById(tagDto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagDto.getId()));
                tagsToAdd.add(tag);
            }

            createdProject.setTags(tagsToAdd);
        }

        try {
            com.google.api.services.drive.model.File folder =
                    googleDriveService.createFolder(createdProject.getName(), googleDriveService.getRootFolderId());
            createdProject.setDriveFolderId(folder.getId());
            createdProject.setDriveFolderLink(folder.getWebViewLink());
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się utworzyć folderu na Dysku Google dla projektu", e);
        }

        Project savedProject = projectRepository.save(createdProject);
        projectAccessService.assignDefaultPermissionOnProjectCreation(createdProject);
        adminService.grantSystemAdminAccess(createdProject);
        return savedProject;
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    @Transactional
    public Project updateProjectInfo(@ProjectId Long id, Project updatedProjectData) {

        Project existingProject = getProjectById(id);

        Company oldCompany = existingProject.getCompany();

        setChangableFields(updatedProjectData, existingProject);

        Company newCompany = existingProject.getCompany();

        if (updatedProjectData.getTags() != null) {
            Set<Tag> updatedTags = new HashSet<>();

            for (var tagDto : updatedProjectData.getTags()) {
                if (tagDto.getId() != null) {
                    Tag tag = tagRepository.findById(tagDto.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Tag not found with ID: " + tagDto.getId()));
                    updatedTags.add(tag);
                }
            }
            existingProject.setTags(updatedTags);
        }

        Project savedProject = projectRepository.save(existingProject);

        if (!Objects.equals(oldCompany, newCompany)) {
            pillarRepository.updateCompanyForProject(id, newCompany);
            itemRepository.updateCompanyForProject(id, newCompany);
        }

        projectAccessService.assignDefaultPermissionOnProjectCreation(existingProject);
        return savedProject;
    }

    @CheckAccess(ProjectPermissions.CAN_DELETE)
    public Project archiveProject(@ProjectId Long id){
        Project archivedProject = getProjectById(id);
        archivedProject.setState("archived");
        return projectRepository.save(archivedProject);
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Project finishProject(@ProjectId Long id){
        Project finishedProject = getProjectById(id);
        finishedProject.setState("finished");
        return projectRepository.save(finishedProject);
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Project addTagToProject(@ProjectId Long projectId, Long tagId) {
        Project project = getProjectById(projectId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag o ID " + tagId + " nie istnieje."));

        project.getTags().add(tag);
        return projectRepository.save(project);
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Project removeTagFromProject(@ProjectId Long projectId, Long tagId) {
        Project project = getProjectById(projectId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag o ID " + tagId + " nie istnieje."));

        project.getTags().remove(tag);
        return projectRepository.save(project);
    }

    public List<Project> searchProjects(SearchingCriteria criteria) {
        return projectRepository.searchProjects(criteria);
    }

    private void checkForProjectDuplicates(Project inputProject){
        String inputName = inputProject.getName();
        String inputPerson = inputProject.getPersonResponsible();
        LocalDate inputDeadline = inputProject.getDeadline();
        Company inputCompany = inputProject.getCompany();
        Integer inputPriority = inputProject.getPriority();

        if(projectRepository.existsByNameAndPersonResponsibleAndStateAndDeadlineAndCompanyAndPriority(inputName,
                inputPerson,"active", inputDeadline,inputCompany,inputPriority)){
            throw new IllegalArgumentException("Identical project already exists!");
        }
    }

    private void setChangableFields(Project inputProject, Project existingProject){
        existingProject.setName(inputProject.getName());
        existingProject.setDeadline(inputProject.getDeadline());
        existingProject.setPriority(inputProject.getPriority());
        existingProject.setPersonResponsible(inputProject.getPersonResponsible());
        existingProject.setCompany(inputProject.getCompany());
    }
}
