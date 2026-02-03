package com.mcdevka.realestate_projects_tracker.domain.item;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@Entity
@ToString(exclude = {"item", "replyTo"})
@Table(name = "item_history")
public class ItemHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime changeDate;

    private String webViewLink; // <-- ZMIANA: Bardziej precyzyjny (to jest link do podglądu)

    private String googleFileId;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String author;

    private boolean edited;

    @JsonProperty("isPinned")
    private boolean isPinned = false;

    @OneToMany(mappedBy = "itemHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<MessageReaction> reactions = new java.util.ArrayList<>();

    private String state = "active";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIncludeProperties({"id", "author", "description"})
    private ItemHistory replyTo;

    @Transient // To pole służy tylko do odebrania ID z frontendu
    @JsonProperty("replyToId")
    private Long replyToId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @JsonIgnore
    private Item item;
}
