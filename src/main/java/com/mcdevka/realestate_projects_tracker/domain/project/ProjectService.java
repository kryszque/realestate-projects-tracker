package com.mcdevka.realestate_projects_tracker.domain.project;

import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarService;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import com.mcdevka.realestate_projects_tracker.domain.tag.TagRepository;
import com.mcdevka.realestate_projects_tracker.domain.user.Role;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.security.AccessControlService;
import com.mcdevka.realestate_projects_tracker.security.annotation.CheckAccess;
import com.mcdevka.realestate_projects_tracker.security.annotation.ProjectId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PillarService pillarService;
    private final TagRepository tagRepository;
    private final AccessControlService accessControlService;

    public List<Project> getAllProjects(){

        User currentUser = accessControlService.getCurrentUser();

        if(currentUser.getRole() == Role.ADMIN){
            return projectRepository.findByStateNot("archived");
        }else{
            return projectRepository.findByStateNotAndCompanyResposible("archived", currentUser.getCompany());
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

        //TODO provide fields that can NOT be null when creating a createdProject + EXCEPTIONS!!
        return projectRepository.save(createdProject);
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Project updateProjectInfo(@ProjectId Long id, Project updatedProjectData) {

        checkForProjectDuplicates(updatedProjectData);

        Project existingProject = getProjectById(id);

        setChangableFields(updatedProjectData, existingProject);

        return projectRepository.save(existingProject);
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

    public List<Project> searchProjects(SearchingCriteria criteria, User currentUser) {
        Specification<Project> spec = ProjectSpecifications.createSearch(criteria, currentUser);
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
