package com.mcdevka.realestate_projects_tracker.domain.item;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findByPillarId(Long pillarId);

    boolean existsByNameAndStateAndCompanyAndPersonResponsibleAndPillarIdAndPriority(String name, String state , Company company, String personResponsible, Long pillarId, Integer priority);

    @Query("SELECT h FROM ItemHistory h WHERE h.id = :historyId AND h.item.id = :itemId")
    Optional<ItemHistory> findHistoryByIdAndItemId(@Param("itemId") Long itemId, @Param("historyId") Long historyId);

    @Modifying // Informuje Springa, że to zapytanie zmienia dane (UPDATE/DELETE)
    @Query("UPDATE Item i SET i.company = :company WHERE i.pillar.project.id = :projectId")
    void updateCompanyForProject(@Param("projectId") Long projectId, @Param("company") Company company);

    @Query("SELECT i FROM Item i WHERE " +
            "(:#{#criteria.name} IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :#{#criteria.name}, '%'))) " +
            // ... inne warunki ...
            // Sprawdź ścieżkę do firmy! (Item -> Pillar -> Project -> Company)
            "AND (:#{#criteria.userAllowedCompanyIds} IS NULL OR i.pillar.project.company.id IN :#{#criteria.userAllowedCompanyIds})")
    List<Item> searchItems(@Param("criteria") SearchingCriteria criteria);
}
