package com.mcdevka.realestate_projects_tracker.domain.pillar;

import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class PillarSpecifications {

    public static Specification<Pillar> createSearch(SearchingCriteria criteria) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Nazwa (LIKE)
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"
                ));
            }

            // 2. Lista Tagów (IN)
            if (!CollectionUtils.isEmpty(criteria.getTags())) {
                // Poprawiono typ generyczny Join<Pillar, Tag>
                Join<Pillar, Tag> tagJoin = root.join("tags", JoinType.LEFT);

                predicates.add(tagJoin.get("name").in(criteria.getTags()));

                // Zapobiegamy duplikatom w wynikach przy wielu tagach
                criteriaQuery.distinct(true);
            }

            // 3. Kontekst PROJEKTU (Pillar -> Project)
            // To pozwala szukać filarów tylko wewnątrz konkretnego projektu
            if (criteria.getProjectId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("project").get("id"),
                        criteria.getProjectId()
                ));
            }

            // (Opcjonalnie) Szukanie po konkretnym ID filaru,
            // jeśli z jakiegoś powodu kryteria to zawierają
            if (criteria.getPillarId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("id"),
                        criteria.getPillarId()
                ));
            }

            // 4. Daty
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

            // 5. Priorytet
            if (criteria.getPriority() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("priority"), criteria.getPriority()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}