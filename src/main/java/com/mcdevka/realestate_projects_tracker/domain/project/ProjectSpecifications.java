package com.mcdevka.realestate_projects_tracker.domain.project;

import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpecifications {

    public static Specification<Project> createSearch(SearchingCriteria criteria) {
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
                Join<Project, Tag> tagJoin = root.join("tags", JoinType.LEFT);

                // Sprawdzamy czy tag projektu jest na liście szukanych tagów
                predicates.add(tagJoin.get("name").in(criteria.getTags()));

                // Unikamy duplikatów w wynikach
                criteriaQuery.distinct(true);
            }

            // 3. Kontekst PROJEKTU
            // Jeśli użytkownik jest wewnątrz projektu (ID=X) i wpisuje w wyszukiwarkę "Budowa",
            // to chcemy sprawdzić, czy TEN konkretny projekt nazywa się "Budowa".
            // To zapobiega zwracaniu innych projektów, gdy jesteśmy w kontekście jednego.
            if (criteria.getProjectId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("id"),
                        criteria.getProjectId()
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