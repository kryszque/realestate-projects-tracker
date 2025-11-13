package com.mcdevka.realestate_projects_tracker.domain.item;

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
@Table(name = "item_history")
public class ItemHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private LocalDate changeDate;

    @Column(nullable = false)
    private String newStatus;

    private String description;

    private LocalDate deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @JsonBackReference
    private Item item;

    public ItemHistory(Item item, String name, String newStatus, String description, LocalDate deadline) {
        this.item = item;
        this.name = name;
        this.newStatus = newStatus;
        this.description = description;
        this.changeDate = LocalDate.now();
        this.deadline = deadline;
    }
}
