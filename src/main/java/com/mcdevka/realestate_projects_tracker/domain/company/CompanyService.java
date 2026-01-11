package com.mcdevka.realestate_projects_tracker.domain.company;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Transactional
    public Company createCompany(Company inputCompany) {
        if(!companyRepository.existsByNameIgnoreCase(inputCompany.getName())) {
            if(inputCompany.getName() == null || inputCompany.getName().isEmpty()) {
                throw new IllegalArgumentException("Company name cannot be empty");
            }
            Company createdCompany = new Company();
            createdCompany.setName(inputCompany.getName());
            return companyRepository.save(createdCompany);
        }
        else throw new IllegalArgumentException("Company with name " + inputCompany.getName() + " already exists!");
    }
}
