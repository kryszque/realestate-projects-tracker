package com.mcdevka.realestate_projects_tracker.item.document;

import com.mcdevka.realestate_projects_tracker.item.Item;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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

}
