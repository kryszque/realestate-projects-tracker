package com.mcdevka.realestate_projects_tracker.domain.project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        try {
            List<Project> allProjects = projectService.getAllProjects();
            return  ResponseEntity.ok(allProjects);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        try{
            Project createdProject = projectService.createProject(project);
            return  ResponseEntity.ok(createdProject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id);
            return ResponseEntity.ok(project);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}") //need to put all values, not provided will be set to null
    public ResponseEntity<Project> updateProjectInfo(@PathVariable Long id, @RequestBody Project updatedProjectData) {
        try {
            Project updatedProject = projectService.updateProjectInfo(id, updatedProjectData);
            return ResponseEntity.ok(updatedProject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<Project> finishProject(@PathVariable Long id) {
        try{
            Project finishedProject = projectService.finishProject(id);
            return ResponseEntity.ok(finishedProject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Project> archiveProject(@PathVariable Long id) {
        try{
            Project archivedProject = projectService.archiveProject(id);
            return ResponseEntity.ok(archivedProject);
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{projectId}/tags/{tagId}")
    public ResponseEntity<Project> addTagToProject(@PathVariable Long projectId,
                                                   @PathVariable Long tagId) {
        try {
            Project updatedProject = projectService.addTagToProject(projectId, tagId);
            return ResponseEntity.ok(updatedProject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{projectId}/tags/{tagId}")
    public ResponseEntity<Project> removeTagFromProject(@PathVariable Long projectId,
                                                        @PathVariable Long tagId) {
        try {
            Project updatedProject = projectService.removeTagFromProject(projectId, tagId);
            return ResponseEntity.ok(updatedProject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

//    @GetMapping("/search")
//    public ResponseEntity<List<Project>> searchProjects(
//            @ModelAttribute ProjectSearchCriteria criteria){
//        List<Project> result = projectService.searchProjects(criteria);
//        return ResponseEntity.ok(result);
//    }
}
