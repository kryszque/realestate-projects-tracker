package com.mcdevka.realestate_projects_tracker.domain.project;

import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarService;
import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import com.mcdevka.realestate_projects_tracker.domain.tag.TagRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PillarService pillarService;
    private final TagRepository tagRepository;

    public ProjectService(ProjectRepository projectRepository, PillarService pillarService, TagRepository tagRepository) {
        this.projectRepository = projectRepository;
        this.pillarService = pillarService;
        this.tagRepository = tagRepository;

    }

    public List<Project> getAllProjects(){
        return projectRepository.findByStateNot("archived");
    }

    public Project getProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + id + " not found!"));
    }

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

        //TODO provide fields that can NOT be null when creating a createdProject + EXCEPTIONS!!
        return projectRepository.save(createdProject);
    }

    public Project updateProjectInfo(Long id, Project updatedProjectData) {

        Project existingProject = getProjectById(id);

        setChangableFields(updatedProjectData, existingProject);

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

        return projectRepository.save(existingProject);
    }

    public Project archiveProject(Long id){
        Project archivedProject = getProjectById(id);
        archivedProject.setState("archived");
        return projectRepository.save(archivedProject);
    }

    public Project finishProject(Long id){
        Project finishedProject = getProjectById(id);
        finishedProject.setState("finished");
        return projectRepository.save(finishedProject);
    }

    public Project addTagToProject(Long projectId, Long tagId) {
        Project project = getProjectById(projectId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag o ID " + tagId + " nie istnieje."));

        project.getTags().add(tag);
        return projectRepository.save(project);
    }

    public Project removeTagFromProject(Long projectId, Long tagId) {
        Project project = getProjectById(projectId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag o ID " + tagId + " nie istnieje."));

        project.getTags().remove(tag);
        return projectRepository.save(project);
    }

    public List<Project> searchProjects(SearchingCriteria criteria) {
        Specification<Project> spec = ProjectSpecifications.createSearch(criteria);
        return projectRepository.findAll(spec);
    }

    private void checkForProjectDuplicates(Project inputProject){
        String inputName = inputProject.getName();
        String inputPlace = inputProject.getPlace();
        String inputContractor = inputProject.getContractor();
        String inputCompanyResposible = inputProject.getCompanyResposible();
        Integer inputPriority = inputProject.getPriority();

        if(projectRepository.existsByNameAndPlaceAndStateAndContractorAndCompanyResposibleAndPriority(inputName,
                inputPlace,"active", inputContractor,inputCompanyResposible,inputPriority)){
            throw new IllegalArgumentException("Identical project already exists!");
        }
    }

    private void setChangableFields(Project inputProject, Project existingProject){
        existingProject.setName(inputProject.getName());
        existingProject.setPlace(inputProject.getPlace());
        existingProject.setPriority(inputProject.getPriority());
        existingProject.setContractor(inputProject.getContractor());
        existingProject.setCompanyResposible(inputProject.getCompanyResposible());
    }
}
