package com.mcdevka.realestate_projects_tracker.domain.user;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.project.ProjectService;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectAccessService;
import com.mcdevka.realestate_projects_tracker.domain.user.dto.UserDetail;
import com.mcdevka.realestate_projects_tracker.domain.user.dto.UserProject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final ProjectAccessService projectAccessService;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public UserDetail getUserDetails(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("User not found!"));

        // Pobieramy projekty (logika bez zmian)
        List<UserProject> projects = projectService.getAllProjects().stream()
                .map(project -> projectAccessService.getUserProject(userId, project.getId()))
                .filter(Objects::nonNull)
                .toList();

        // ZMIANA: Mapujemy Set<Company> na List<String> (nazwy firm)
        List<String> companyNames = user.getCompanies().stream()
                .map(Company::getName)
                .collect(Collectors.toList());

        // Przekazujemy listę nazw firm do DTO
        return new UserDetail(
                user.getId(),
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getRole().name(),
                companyNames, // <--- Tutaj przekazujemy listę zamiast pojedynczego stringa
                projects
        );
    }

    @Transactional
    public UserDetail updateCurrentUser(Long userId, User requestData) {
        // 1. Pobieramy aktualnego użytkownika z bazy
        User userFromDb = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));

        // 2. Walidacja i zmiana maila
        // Używamy requestData (to co przyszło z frontendu) jako źródła danych
        if (requestData.getEmail() != null && !requestData.getEmail().isBlank()) {
            // Jeśli mail jest inny niż obecny, sprawdzamy czy nie jest zajęty
            if (!requestData.getEmail().equals(userFromDb.getEmail())) {
                if (userRepository.findByEmail(requestData.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("Email " + requestData.getEmail() + " jest już zajęty.");
                }
                userFromDb.setEmail(requestData.getEmail());
            }
        }

        // 3. Aktualizacja imienia i nazwiska
        // Sprawdzamy czy przyszły w requestData, jeśli tak - nadpisujemy
        if (requestData.getFirstname() != null && !requestData.getFirstname().isBlank()) {
            userFromDb.setFirstname(requestData.getFirstname());
        }
        if (requestData.getLastname() != null && !requestData.getLastname().isBlank()) {
            userFromDb.setLastname(requestData.getLastname());
        }

        // 4. Zapisujemy zmiany
        userRepository.save(userFromDb);

        // 5. Zwracamy odświeżony widok (UserDetail)
        return getUserDetails(userId);
    }
}
