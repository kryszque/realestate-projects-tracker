package com.mcdevka.realestate_projects_tracker.domain.pillar;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString(exclude = {"project", "items"})
@EqualsAndHashCode(of = {"id"})
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
    @OneToMany(
           mappedBy = "pillar",
            cascade = CascadeType.ALL,
             orphanRemoval = true
    )
    @JsonManagedReference
    List<Item> items;
}
