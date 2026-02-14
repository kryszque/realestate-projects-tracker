package com.mcdevka.realestate_projects_tracker.domain.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name ="users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("firstName") // Mapuje klucz "firstname" z JSONa
    private String firstname;

    @JsonProperty("lastName")
    private String lastname;
    @Column(unique = true) // email is going to be the login - it has to be unique
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(fetch = FetchType.EAGER) // EAGER: pobierz firmy od razu przy pobieraniu usera (przydatne przy logowaniu)
    @JoinTable(
            name = "user_companies",               // Nazwa tabeli łączącej w bazie
            joinColumns = @JoinColumn(name = "user_id"),         // Klucz usera
            inverseJoinColumns = @JoinColumn(name = "company_id") // Klucz firmy
    )
    private Set<Company> companies = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
