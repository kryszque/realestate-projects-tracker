package com.mcdevka.realestate_projects_tracker.domain.tag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"projects"})
@EqualsAndHashCode(of = {"id"})
@Entity
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String state = "active";

    private String color;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Pillar> pillars = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Item> items = new HashSet<>();

}
