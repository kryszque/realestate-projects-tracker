package com.mcdevka.realestate_projects_tracker.domain.company;

import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {
    boolean existsByNameIgnoreCase(String name);
}
