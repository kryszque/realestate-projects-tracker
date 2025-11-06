package com.mcdevka.realestate_projects_tracker.item;


import com.fasterxml.jackson.annotation.JsonBackReference;
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
public abstract class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String state;
    private LocalDate addDate;
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
