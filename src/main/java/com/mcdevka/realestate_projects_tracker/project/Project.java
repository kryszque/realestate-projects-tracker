package com.mcdevka.realestate_projects_tracker.project;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mcdevka.realestate_projects_tracker.pillar.Pillar;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import com.mcdevka.realestate_projects_tracker.tag.Tag;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"pillars"})
@EqualsAndHashCode(of = {"id"})
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String place;

    @ElementCollection
    @CollectionTable(name = "project_parties", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "party_name")
    private List<String> partiesInvolved;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "project_tags",  // Nazwa Twojej nowej, pośredniej tabeli
            joinColumns = @JoinColumn(name = "project_id"), // Kolumna wskazująca na Project
            inverseJoinColumns = @JoinColumn(name = "tag_id") // Kolumna wskazująca na Tag
    )
    @JsonManagedReference
    private Set<Tag> tags = new HashSet<>();
    // spolka (dostępność)
    // pomysl tylko nazwa projekt wymagania

    private LocalDate startDate;
    private String state;

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<Pillar> pillars;

}
