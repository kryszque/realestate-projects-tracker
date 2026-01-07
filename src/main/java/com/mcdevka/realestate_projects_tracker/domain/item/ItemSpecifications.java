package com.mcdevka.realestate_projects_tracker.domain.item;

import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemSpecifications {

    public static Specification<Item> createSearch(SearchingCriteria criteria) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Wyszukiwanie po nazwie (LIKE)
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"
                ));
            }

            // 2. Wyszukiwanie po liście tagów (IN)
            if (!CollectionUtils.isEmpty(criteria.getTags())) {
                Join<Item, Tag> tagJoin = root.join("tags", JoinType.LEFT);

                // Sprawdź czy nazwa tagu znajduje się na liście przekazanej w kryteriach
                predicates.add(tagJoin.get("name").in(criteria.getTags()));

                // Ważne: przy joinach mogą pojawić się duplikaty itemów, więc wymuszamy distinct
                criteriaQuery.distinct(true);
            }

            // 3. Kontekst PROJEKTU (Item -> Pillar -> Project)
            if (criteria.getProjectId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("pillar").get("project").get("id"),
                        criteria.getProjectId()
                ));
            }

            // 4. Kontekst FILARU (Item -> Pillar)
            if (criteria.getPillarId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("pillar").get("id"),
                        criteria.getPillarId()
                ));
            }

            // 5. Daty
            if (criteria.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("startDate"), criteria.getCreatedAfter()
                ));
            }

            if (criteria.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("startDate"), criteria.getCreatedBefore()
                ));
            }

            // 6. Priorytet
            if (criteria.getPriority() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("priority"), criteria.getPriority()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}