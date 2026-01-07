package com.mcdevka.realestate_projects_tracker.domain.pillar;


import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import com.mcdevka.realestate_projects_tracker.domain.tag.TagRepository;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
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

    public PillarService(PillarRepository pillarRepository, ProjectRepository projectRepository,
                         TagRepository tagRepository) {
        this.pillarRepository = pillarRepository;
        this.projectRepository = projectRepository;
        this.tagRepository = tagRepository;
    }

    public List<Pillar> initializeDefaultPillars(Project project){
        List<Pillar> threePillars = new ArrayList<>();
        Pillar p1 = new Pillar();
        p1.setCompanyResposible(project.getCompanyResposible());
        p1.setName("Design");
        p1.setStartDate(LocalDate.now());
        p1.setState("active");
        p1.setProject(project);
        threePillars.add(p1);

        Pillar p2 = new Pillar();
        p2.setCompanyResposible(project.getCompanyResposible());
        p2.setName("Relacje");
        p2.setStartDate(LocalDate.now());
        p2.setState("active");
        p2.setProject(project);
        threePillars.add(p2);

        Pillar p3 = new Pillar();
        p3.setCompanyResposible(project.getCompanyResposible());
        p3.setName("Prawo");
        p3.setStartDate(LocalDate.now());
        p3.setState("active");
        p3.setProject(project);
        threePillars.add(p3);

        Pillar p4 = new Pillar();
        p4.setCompanyResposible(project.getCompanyResposible());
        p4.setName("INFO");
        p4.setStartDate(LocalDate.now());
        p4.setState("active");
        p4.setProject(project);
        threePillars.add(p4);

        return threePillars;
    }

    @CheckAccess(ProjectPermissions.CAN_VIEW)
    public Pillar getPillarById(@ProjectId Long projectId, Long id){
        Pillar pillar = pillarRepository.findByIdAndStateNot(id, "archived")
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
        String inputCompany = project.getCompanyResposible();

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
        newPillar.setCompanyResposible(inputCompany);
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
        Pillar archivedPillar = validateProjectId(projectId, pillarId);
        archivedPillar.setState("archived");
        return pillarRepository.save(archivedPillar);
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
        Specification<Pillar> spec = PillarSpecifications.createSearch(criteria);
        return pillarRepository.findAll(spec);
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
}
