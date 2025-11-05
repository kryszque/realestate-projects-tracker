package com.mcdevka.realestate_projects_tracker.project;

import com.mcdevka.realestate_projects_tracker.pillar.PillarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PillarService pillarService;
    @Autowired
    public ProjectService(ProjectRepository projectRepository, PillarService pillarService) {
        this.projectRepository = projectRepository;
        this.pillarService = pillarService;
    }

    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + id + " not found!"));
    }

    public Project createProject(Project inputProject){
        Project createdProject = new Project();

        createdProject.setName(inputProject.getName());
        createdProject.setTag(inputProject.getTag()); //can be null
        createdProject.setPlace(inputProject.getPlace());
        createdProject.setPartiesInvolved(inputProject.getPartiesInvolved());

        createdProject.setStartDate(LocalDate.now());
        createdProject.setState("active");
        createdProject.setPillars(pillarService.initializeDefaultPillars(createdProject));

        //TODO provide fields that can NOT be null when creating a createdProject + EXCEPTIONS!!
        return projectRepository.save(createdProject);
    }

    public Project updateProjectInfo(Long id, Project updatedProjectData) {
        Project existingProject = getProjectById(id);

        existingProject.setName(updatedProjectData.getName());
        existingProject.setTag(updatedProjectData.getTag());
        existingProject.setPlace(updatedProjectData.getPlace());
        existingProject.setPartiesInvolved(updatedProjectData.getPartiesInvolved());
        existingProject.setStartDate(updatedProjectData.getStartDate());
        existingProject.setState(updatedProjectData.getState());

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

    public boolean compareInfo(Project p, Object o){
        throw new IllegalArgumentException("not implemented yet");
    }
}
