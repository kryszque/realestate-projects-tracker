package com.mcdevka.realestate_projects_tracker.domain.pillar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"project", "items", "tags"})
@EqualsAndHashCode(of = {"id"})
@Entity
public class Pillar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer priority;

    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    private String name;
    private LocalDate startDate;
    private String state;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties("pillars")
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
    @JsonIgnoreProperties("pillar")
    @Where(clause = "state != 'archived'")
    List<Item> items;
    private String driveFolderId;
    private String driveFolderLink;
}
