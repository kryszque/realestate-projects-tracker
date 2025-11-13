package com.mcdevka.realestate_projects_tracker.domain.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByPillarId(Long pillarId);

    boolean existsByNameAndStateAndPillarId(String name, String state, Long pillarId);

    // ✨ NOWA METODA: Sprawdzanie dla aktualizacji (update)
    // Sprawdza to samo, ale ignoruje podane ID (czyli sam edytowany item)
    boolean existsByNameAndStateAndPillarIdAndIdNot(String name, String state, Long pillarId, Long id);
}
