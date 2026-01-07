package com.mcdevka.realestate_projects_tracker.domain.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "message_reactions")
public class MessageReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String emojiCode;

    private String userName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_history_id")
    @JsonIgnore
    private ItemHistory itemHistory;

    public MessageReaction(String emojiCode, String userName, ItemHistory itemHistory) {
        this.emojiCode = emojiCode;
        this.userName = userName;
        this.itemHistory = itemHistory;
    }
}