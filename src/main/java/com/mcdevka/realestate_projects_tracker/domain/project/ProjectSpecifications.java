package com.mcdevka.realestate_projects_tracker.domain.project;

import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpecifications {
    public static Specification<Project> createSearch(SearchingCriteria criteria) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(criteria.getName() != null &&  !criteria.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" +  criteria.getName().toLowerCase() + "%"
                ));
            }

            if(criteria.getTagName() != null && !criteria.getTagName().isEmpty()) {
                Join<Project, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(tagJoin.get("name")),
                        "%" +  criteria.getTagName().toLowerCase() + "%"
                ));
                criteriaQuery.distinct(true);
            }

            if(criteria.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("startDate"), criteria.getCreatedAfter()
                ));
            }

            if(criteria.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("startDate"), criteria.getCreatedBefore()
                ));
            }

            if(criteria.getPriority() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("priority"), criteria.getPriority()
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
