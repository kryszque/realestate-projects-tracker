package com.mcdevka.realestate_projects_tracker.config;


import com.mcdevka.realestate_projects_tracker.domain.user.Role;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.default-admin.firstName}")
    private String adminFirstName;
    @Value("${application.default-admin.lastName}")
    private String adminLastName;
    @Value("${application.default-admin.mail}")
    private String adminMail;
    @Value("${application.default-admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.findByEmail(adminMail).isEmpty()) {

            String encodedPassword = passwordEncoder.encode(adminPassword);
            User admin =User.builder()
                    .firstname(adminFirstName)
                    .lastname(adminLastName)
                    .email(adminMail)
                    .password(encodedPassword)
                    .role(Role.ADMIN)
                    .company("SYSTEM")
                    .build();
            userRepository.save(admin);
            System.out.println("Admin has been created");
        }else{
            System.out.println("Admin has already been created");
        }
    }

}
