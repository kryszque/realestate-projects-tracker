package com.mcdevka.realestate_projects_tracker.domain.pillar;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"project", "items"})
@EqualsAndHashCode(of = {"id"})
@Entity
public class Pillar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer priority;

    private String name;
    private LocalDate startDate;
    private String state;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private Project project;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "pillar_tags",
            joinColumns = @JoinColumn(name = "pillar_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(
           mappedBy = "pillar",
            cascade = CascadeType.ALL,
             orphanRemoval = true
    )
    @JsonManagedReference
    List<Item> items;
}
