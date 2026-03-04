package com.mcdevka.realestate_projects_tracker.domain.item;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"pillar", "historyEntries", "tags"})
@EqualsAndHashCode(of = {"id"})
@Entity

public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer priority;

    @Column(nullable = false)
    private String name;
    private String state = "active";

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    private String personResponsible;

    @Column(nullable = false, updatable = false)
    private LocalDate startDate;
    private LocalDate deadline;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pillar_id")
    @JsonIgnoreProperties("items")
    private Pillar pillar;

    @OneToMany(
            mappedBy = "item",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Where(clause = "state != 'archived'")
    private List<ItemHistory> historyEntries = new ArrayList<>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "item_tags",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }
    private String driveFolderId;
    private String driveFolderLink;

    @Transient
    @JsonProperty("customDriveFolderId")
    private String customDriveFolderId;
}
