package com.mcdevka.realestate_projects_tracker.pillar;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mcdevka.realestate_projects_tracker.project.Project;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Objects;

@Data
@Entity
public class Pillar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String state;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private Project project;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pillar pillar = (Pillar) o;
        return name.equals(pillar.name) &&  state.equals(pillar.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, state);
    }
}
