package com.mcdevka.realestate_projects_tracker.domain.pillar;


import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import com.mcdevka.realestate_projects_tracker.domain.tag.TagRepository;
import com.mcdevka.realestate_projects_tracker.infrastructure.drive.GoogleDriveService;
import com.mcdevka.realestate_projects_tracker.security.annotation.CheckAccess;
import com.mcdevka.realestate_projects_tracker.security.annotation.ProjectId;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PillarService {

    private final PillarRepository pillarRepository;
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final GoogleDriveService googleDriveService;

    public PillarService(PillarRepository pillarRepository, ProjectRepository projectRepository,
                         TagRepository tagRepository, GoogleDriveService googleDriveService) {
        this.pillarRepository = pillarRepository;
        this.projectRepository = projectRepository;
        this.tagRepository = tagRepository;
        this.googleDriveService = googleDriveService;
    }

    public List<Pillar> initializeDefaultPillars(Project project){
        List<Pillar> pillars = new ArrayList<>();

        String[] defaultNames = {"Design", "Relacje", "Prawo", "INFO"};

        for (String name : defaultNames) {
            Pillar p = new Pillar();
            p.setCompany(project.getCompany());
            p.setName(name);
            p.setStartDate(LocalDate.now());
            p.setState("active");
            p.setProject(project);

            try {
                if (project.getDriveFolderId() != null) {
                    com.google.api.services.drive.model.File folder =
                            googleDriveService.createFolder(p.getName(), project.getDriveFolderId());
                    p.setDriveFolderId(folder.getId());
                    p.setDriveFolderLink(folder.getWebViewLink());
                }
            } catch (Exception e) {
                System.err.println("Couldn't create drive folder for this module " + name + ": " + e.getMessage());
            }

            pillars.add(p);
        }

        return pillars;
    }

    @CheckAccess(ProjectPermissions.CAN_VIEW)
    public Pillar getPillarById(@ProjectId Long projectId, Long id){
        Pillar pillar = pillarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pillar with id " + id + " not found!"));
        if (!pillar.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Pillar with ID " + id + " is not in pillar/project path");
        }
        return pillar;
    }

    @CheckAccess(ProjectPermissions.CAN_VIEW)
    public List<Pillar> getAllPillarsForAProject(@ProjectId Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + projectId + " not found!"));
        return project.getPillars();
    }

    @CheckAccess(ProjectPermissions.CAN_CREATE)
    public Pillar createPillar(@ProjectId Long projectId, Pillar inputPillar){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + projectId + " not found!"));

        String inputName = inputPillar.getName();
        Integer inputPriority = inputPillar.getPriority();
        LocalDate inputDeadline = inputPillar.getDeadline();
        Company inputCompany = project.getCompany();

        if(pillarRepository.existsByNameAndStateAndProjectIdAndPriority(inputName, "active",  projectId, inputPriority)){
            throw new  IllegalArgumentException("Pillar with name " + inputName + " already exists in " +
                                                "this project!");
        }
        Pillar newPillar = new  Pillar();
        newPillar.setProject(project);
        newPillar.setName(inputName);

        newPillar.setPriority(inputPillar.getPriority());
        newPillar.setStartDate(LocalDate.now());
        newPillar.setState("active");
        newPillar.setCompany(inputCompany);
        newPillar.setDeadline(inputDeadline);

        if (inputPillar.getTags() != null && !inputPillar.getTags().isEmpty()) {
            Set<Tag> tagsToAdd = new HashSet<>();

            for (var tagDto : inputPillar.getTags()) {
                Tag tag = tagRepository.findById(tagDto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagDto.getId()));
                tagsToAdd.add(tag);
            }

            newPillar.setTags(tagsToAdd);
        }

        try {
            com.google.api.services.drive.model.File folder =
                    googleDriveService.createFolder(newPillar.getName(), project.getDriveFolderId());
            newPillar.setDriveFolderId(folder.getId());
            newPillar.setDriveFolderLink(folder.getWebViewLink());
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się utworzyć folderu na Dysku Google dla filaru", e);
        }

        return pillarRepository.save(newPillar);
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Pillar updatePillarInfo(@ProjectId Long projectId, Long pillarId, Pillar inputPillar){
        projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + projectId + " not found!"));

        String inputName = inputPillar.getName();

        Pillar updatedPillar = validateProjectId(projectId, pillarId);
        updatedPillar.setName(inputName);
        updatedPillar.setPriority(inputPillar.getPriority());
        updatedPillar.setDeadline(inputPillar.getDeadline());

        if (inputPillar.getTags() != null) {
            Set<Tag> updatedTags = new HashSet<>();

            for (var tagDto : inputPillar.getTags()) {
                if (tagDto.getId() != null) {
                    Tag tag = tagRepository.findById(tagDto.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Tag not found with ID: " + tagDto.getId()));
                    updatedTags.add(tag);
                }
            }

            updatedPillar.setTags(updatedTags);
        }

        return pillarRepository.save(updatedPillar);
    }

    @CheckAccess(ProjectPermissions.CAN_DELETE)
    public Pillar archivePillar(@ProjectId Long projectId, Long pillarId){
        Pillar archivedPillar = validateProjectId(projectId, pillarId); //

        // Dodanie prefiksu do nazwy
        if (!archivedPillar.getName().startsWith("[zarchiwizowany]")) {
            archivedPillar.setName("[zarchiwizowany] " + archivedPillar.getName());
        }

        archivedPillar.setState("archived"); //
        return pillarRepository.save(archivedPillar); //
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Pillar finishPillar(@ProjectId Long projectId, Long pillarId){
        Pillar finishedPillar = validateProjectId(projectId, pillarId);
        finishedPillar.setState("finished");
        return pillarRepository.save(finishedPillar);
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Pillar addTagToPillar(@ProjectId Long projectId, Long pillarId, Long tagId){
        Pillar pillar = getPillarById(projectId, pillarId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag o ID " + tagId + " nie istnieje."));

        pillar.getTags().add(tag);
        return pillarRepository.save(pillar);
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Pillar removeTagFromPillar(@ProjectId Long projectId, Long pillarId, Long tagId){
        Pillar pillar = getPillarById(projectId, pillarId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag o ID " + tagId + " nie istnieje."));

        pillar.getTags().remove(tag);
        return pillarRepository.save(pillar);
    }

    public List<Pillar> searchPillars(SearchingCriteria criteria){
        return pillarRepository.searchPillars(criteria);
    }

    private Pillar validateProjectId(Long projectId, Long pillarId){
        Pillar pillar = getPillarByIdUnfiltered(pillarId);
        if(!pillar.getProject().getId().equals(projectId)){
            throw new IllegalArgumentException("Pillat with id" +
                    pillarId + " doesn't belong to project id: " + projectId);
        }
        return pillar;
    }

    public Pillar getPillarByIdUnfiltered(Long id){
        // Używamy domyślnego findById, które pobiera WSZYSTKO z bazy
        return pillarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pillar with ID " + id + " does not exist!"));
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Pillar unarchivePillar(@ProjectId Long projectId, Long pillarId){
        Pillar pillar = validateProjectId(projectId, pillarId);

        // Usuń prefiks, jeśli istnieje
        if (pillar.getName().startsWith("[zarchiwizowany] ")) {
            pillar.setName(pillar.getName().replaceFirst("\\[zarchiwizowany\\] ", ""));
        }

        pillar.setState("active");
        return pillarRepository.save(pillar);
    }
}
