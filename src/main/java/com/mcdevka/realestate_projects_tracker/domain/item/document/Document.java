package com.mcdevka.realestate_projects_tracker.domain.item.document;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue("DOCUMENT")
public class Document extends Item {
    private String status;
    private LocalDate lastChangeDate;
    private LocalDate deadline;

    @Column(nullable = false)
    private String webViewLink; // <-- ZMIANA: Bardziej precyzyjny (to jest link do podglądu)

    @Column(nullable = false) // <-- DODANE: To jest ID pliku na Dysku Google
    private String googleFileId;

    @OneToMany(
            mappedBy = "document",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<DocumentHistory> historyEntries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.lastChangeDate = LocalDate.now();
    }
}
