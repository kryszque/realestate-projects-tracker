package com.mcdevka.realestate_projects_tracker.project;

import com.mcdevka.realestate_projects_tracker.pillar.PillarService;
import com.mcdevka.realestate_projects_tracker.tag.Tag;
import com.mcdevka.realestate_projects_tracker.tag.TagRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
        return projectRepository.findAll();
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
        createdProject.setPillars(pillarService.initializeDefaultPillars(createdProject));

        //TODO provide fields that can NOT be null when creating a createdProject + EXCEPTIONS!!
        return projectRepository.save(createdProject);
    }

    public Project updateProjectInfo(Long id, Project updatedProjectData) {

        checkForProjectDuplicates(updatedProjectData);

        Project existingProject = getProjectById(id);

        setChangableFields(updatedProjectData, existingProject);

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

    private void checkForProjectDuplicates(Project inputProject){
        String inputName = inputProject.getName();
        String inputPlace = inputProject.getPlace();
        String inputContractor = inputProject.getContractor();
        String inputCompanyResposible = inputProject.getCompanyResposible();

        if(projectRepository.existsByNameAndPlaceAndStateAndContractorAndCompanyResposible(inputName,
                inputPlace,"active", inputContractor,inputCompanyResposible)){
            throw new IllegalArgumentException("Identical project already exists!");
        }
    }

    private void setChangableFields(Project inputProject, Project existingProject){
        existingProject.setName(inputProject.getName());
        existingProject.setPlace(inputProject.getPlace());
        existingProject.setContractor(inputProject.getContractor());
        existingProject.setCompanyResposible(inputProject.getCompanyResposible());
    }
}
