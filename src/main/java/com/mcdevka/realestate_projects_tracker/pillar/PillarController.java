package com.mcdevka.realestate_projects_tracker.pillar;


import com.mcdevka.realestate_projects_tracker.project.Project;
import com.mcdevka.realestate_projects_tracker.project.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/projects/{projectId}/pillars")
public class PillarController {
    private final PillarService pillarService;
    private final ProjectService projectService;

    public PillarController(PillarService pillarService, ProjectService projectService) {
        this.pillarService = pillarService;
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Pillar>> getAllPillarsForAProject(@PathVariable Long projectId){
        try {
            List<Pillar> pillars = pillarService.getAllPillarsForAProject(projectId);
            return ResponseEntity.ok(pillars);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public Pillar createPillarForAProject(@PathVariable Long projectId, @RequestBody Pillar pillar) {
        return pillarService.createPillar(projectId, pillar);
    }

    @PutMapping("/{pillarId}")
    public ResponseEntity<Pillar> updatePillarInfoForAProject(@PathVariable Long projectId,
                                                              @PathVariable Long pillarId,
                                                              @RequestBody Pillar pillar) {
        try {
            Pillar updatedPillar = pillarService.updatePillarInfo(projectId, pillarId, pillar);
            return ResponseEntity.ok(updatedPillar);
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{pillarId}/archive")
    public ResponseEntity<Pillar> archivePillarForAProject(@PathVariable Long projectId,
                                                           @PathVariable Long pillarId) {
        try{
            Pillar archivedPillar = pillarService.archivePillar(projectId, pillarId);
            return ResponseEntity.ok(archivedPillar);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{pillarId}/finish")
        public ResponseEntity<Pillar> finishPillar(@PathVariable Long projectId,
                                                   @PathVariable Long pillarId){
        try{
            Pillar  finishedPillar = pillarService.finishPillar(projectId, pillarId);
            return ResponseEntity.ok(finishedPillar);
        }  catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
