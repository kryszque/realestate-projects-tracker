package com.mcdevka.realestate_projects_tracker.domain.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findByPillarId(Long pillarId);

    boolean existsByNameAndStateAndPillarIdAndPriority(String name, String state, Long pillarId, Integer priority);
}
