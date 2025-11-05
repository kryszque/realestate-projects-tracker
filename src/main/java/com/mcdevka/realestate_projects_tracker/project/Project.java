package com.mcdevka.realestate_projects_tracker.project;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mcdevka.realestate_projects_tracker.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.pillar.PillarService;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String tag; //!!later make class Tag
    private String place;

    @ElementCollection
    @CollectionTable(name = "project_parties", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "party_name")
    private List<String> partiesInvolved;

    private LocalDate startDate;
    private String state;

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<Pillar> pillars;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return name.equals(project.name) && place.equals(project.place) &&
                partiesInvolved.equals(project.partiesInvolved) && startDate.equals(project.startDate)
                && state.equals(project.state) && pillars.equals(project.pillars);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, place, partiesInvolved, startDate, state, pillars);
        //TODO should i use id in hash func?
    }
}
