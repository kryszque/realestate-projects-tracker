package com.mcdevka.realestate_projects_tracker.domain.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findByPillarId(Long pillarId);

    boolean existsByNameAndStateAndCompanyResposibleAndPersonResponsibleAndPillarIdAndPriority(String name, String state ,String companyResposible, String personResponsible, Long pillarId, Integer priority);

    @Query("SELECT h FROM ItemHistory h WHERE h.id = :historyId AND h.item.id = :itemId")
    Optional<ItemHistory> findHistoryByIdAndItemId(@Param("itemId") Long itemId, @Param("historyId") Long historyId);
}
