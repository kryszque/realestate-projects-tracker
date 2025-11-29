package com.mcdevka.realestate_projects_tracker.domain.project;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"pillars", "tags"})
@EqualsAndHashCode(of = {"id"})
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer priority;

    @Column(nullable = false)
    private String name;

    private String place;

    private String contractor;

    private String companyResposible;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "project_tags",  // Nazwa Twojej nowej, pośredniej tabeli
            joinColumns = @JoinColumn(name = "project_id"), // Kolumna wskazująca na Project
            inverseJoinColumns = @JoinColumn(name = "tag_id") // Kolumna wskazująca na Tag
    )
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
    @Where(clause = "state != 'archived'")
    private List<Pillar> pillars;

}
