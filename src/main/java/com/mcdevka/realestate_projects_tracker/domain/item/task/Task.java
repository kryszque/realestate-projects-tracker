package com.mcdevka.realestate_projects_tracker.domain.item.task;

import com.mcdevka.realestate_projects_tracker.domain.item.Item;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("task")
public class Task extends Item {
    LocalDate deadline;
}
