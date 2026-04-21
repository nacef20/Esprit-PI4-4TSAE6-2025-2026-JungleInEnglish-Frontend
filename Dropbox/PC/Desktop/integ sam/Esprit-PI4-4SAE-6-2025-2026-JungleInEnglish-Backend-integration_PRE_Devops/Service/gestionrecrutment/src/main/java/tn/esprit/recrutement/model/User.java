package tn.esprit.recrutement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import jakarta.persistence.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Roles role;

    /** Keycloak user UUID — links this local record to the Keycloak identity */
    private String keycloakUserId;

    // Explicit getters for Spring Security / when Lombok annotation processing is not run
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Roles getRole() { return role; }
}
