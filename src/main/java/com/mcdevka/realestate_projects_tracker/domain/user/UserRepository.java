package com.mcdevka.realestate_projects_tracker.domain.user;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findAllByCompaniesContaining(Company company);
}
