package com.mcdevka.realestate_projects_tracker.pillar;


import com.mcdevka.realestate_projects_tracker.project.Project;
import com.mcdevka.realestate_projects_tracker.project.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PillarService {

    private final PillarRepository pillarRepository;
    private final ProjectRepository projectRepository;

    @Autowired
    public PillarService(PillarRepository pillarRepository, ProjectRepository projectRepository) {
        this.pillarRepository = pillarRepository;
        this.projectRepository = projectRepository;
    }

    public List<Pillar> initializeDefaultPillars(Project project){
        List<Pillar> threePillars = new ArrayList<>();
        Pillar p1 = new Pillar();
        p1.setName("Design");
        p1.setState("active");
        p1.setProject(project);
        threePillars.add(p1);

        Pillar p2 = new Pillar();
        p2.setName("Commercialization");
        p2.setState("active");
        p2.setProject(project);
        threePillars.add(p2);

        Pillar p3 = new Pillar();
        p3.setName("Sale");
        p3.setState("active");
        p3.setProject(project);
        threePillars.add(p3);

        return threePillars;
    }

    public Pillar getPillarById(Long id){
        return pillarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pillar with id " + id + " not found!"));
    }

    public List<Pillar> getAllPillarsForAProject(Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + projectId + " not found!"));
        return project.getPillars();
    }

    public Pillar createPillar(Long projectId, Pillar inputPillar){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with ID " + projectId + " not found!"));

        String inputName = inputPillar.getName();

        if(pillarRepository.existsByNameAndStatusAndProjectId(inputName, "active",  projectId)){
            throw new  IllegalArgumentException("Pillar with name " + inputName + " already exists in " +
                                                "this project!");
        }
        Pillar newPillar = new  Pillar();
        newPillar.setProject(project);
        newPillar.setName(inputName);
        newPillar.setState("active");

        return pillarRepository.save(newPillar);
    }

    public Pillar updatePillarInfo(Long projectId, Long pillarId, Pillar inputPillar){
        Pillar updatedPillar = validateProjectId(projectId, pillarId);
        String inputName = inputPillar.getName();
        if(pillarRepository.existsByNameAndStatusAndProjectId(inputName, "active",  projectId)){
            throw new  IllegalArgumentException("Pillar with name " + inputName + " already exists " +
                    "in this project!");
        }
        updatedPillar.setName(inputName);
        return pillarRepository.save(updatedPillar);
    }

    public Pillar archivePillar(Long projectId, Long pillarId){
        Pillar archivedPillar = validateProjectId(projectId, pillarId);
        archivedPillar.setState("archived");
        return pillarRepository.save(archivedPillar);
    }

    public Pillar finishPillar(Long projectId, Long pillarId){
        Pillar finishedPillar = validateProjectId(projectId, pillarId);
        finishedPillar.setState("finished");
        return pillarRepository.save(finishedPillar);
    }

    private Pillar validateProjectId(Long projectId, Long pillarId){
        Pillar pillar = getPillarById(pillarId);
        if(!pillar.getProject().getId().equals(projectId)){
            throw new IllegalArgumentException("Pillat with id" +
                    pillarId + " doesn't belong to project id: " + projectId);
        }
        return pillar;
    }
}
