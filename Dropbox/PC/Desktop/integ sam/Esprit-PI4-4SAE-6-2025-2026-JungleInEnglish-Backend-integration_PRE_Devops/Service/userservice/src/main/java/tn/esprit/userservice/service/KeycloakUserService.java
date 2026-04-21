package tn.esprit.userservice.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
public class KeycloakUserService {

    private final Keycloak keycloak;

    public KeycloakUserService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public UserRepresentation getUserById(String userId) {
        return keycloak
                .realm("jungleinenglish-realm")
                .users()
                .get(userId)
                .toRepresentation();
    }
}
