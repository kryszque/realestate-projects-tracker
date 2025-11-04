package com.mcdevka.realestate_projects_tracker.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
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
    public Project createProject(@RequestBody Project project) {
        return projectService.createProject(project);
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
    public ResponseEntity<Project> finishProject(@PathVariable Long id, @RequestBody Project projectData) {
        try{
            Project finishedProject = projectService.finishProject(id);
            return ResponseEntity.ok(finishedProject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Project> archiveProject(@PathVariable Long id, @RequestBody Project projectData) {
        try{
            Project archivedProject = projectService.archiveProject(id);
            return ResponseEntity.ok(archivedProject);
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
