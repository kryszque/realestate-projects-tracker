package com.mcdevka.realestate_projects_tracker.item;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mcdevka.realestate_projects_tracker.item.document.Document;
import com.mcdevka.realestate_projects_tracker.item.meeting.Meeting;
import com.mcdevka.realestate_projects_tracker.item.task.Task;
import com.mcdevka.realestate_projects_tracker.pillar.Pillar;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@ToString(exclude = {"pillar"})
@EqualsAndHashCode(of = {"id"})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type")

// 1. Mówimy Jacksonowi, że ta klasa ma podtypy
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,         // Użyj nazw logicznych (np. "textNote")
        include = JsonTypeInfo.As.PROPERTY, // Spodziewaj się pola w JSON
        property = "type"                   // To pole będzie nazywać się "type"
)

@JsonSubTypes({
        @JsonSubTypes.Type(value = Task.class, name = "textNote"),
        @JsonSubTypes.Type(value = Document.class, name = "document"),
        @JsonSubTypes.Type(value = Meeting.class, name = "meeting")
})

public abstract class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String state = "active";
    private LocalDate addDate;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pillar_id")
    @JsonBackReference
    private Pillar pillar;

    public boolean equalsInfo(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return name.equals(item.name) && state.equals(item.state) &&
                addDate.equals(item.addDate) && description.equals(item.description);
    }

}
