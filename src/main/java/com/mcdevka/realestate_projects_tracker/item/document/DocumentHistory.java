package com.mcdevka.realestate_projects_tracker.item.document;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "document_history")
public class DocumentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate changeDate;

    private String oldStatus;

    @Column(nullable = false)
    private String newStatus;

    private String description;

    private LocalDate deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @JsonBackReference
    private Document document;

    public DocumentHistory(Document document, String oldStatus, String newStatus, String description, LocalDate deadline) {
        this.document = document;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.description = description;
        this.changeDate = LocalDate.now();
        this.deadline = deadline;
    }
}
