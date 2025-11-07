package com.mcdevka.realestate_projects_tracker.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByPillarId(Long pillarId);
}
