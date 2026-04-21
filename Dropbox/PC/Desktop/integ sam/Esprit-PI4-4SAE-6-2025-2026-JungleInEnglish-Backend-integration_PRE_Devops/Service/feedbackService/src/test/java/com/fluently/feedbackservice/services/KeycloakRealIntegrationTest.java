package com.fluently.feedbackservice.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Boots up a larger chunk of the application
@SpringBootTest
@TestPropertySource(properties = {
        "keycloak.auth-server-url=http://localhost:8085",
        "keycloak.realm=jungleinenglish-realm",
        "keycloak.client-id=jungleinenglish-backend",
        "keycloak.client-secret=pQJW27kylPU2e1yo0CW4voEBy6bQqh8Z"
})
public class KeycloakRealIntegrationTest {

    @Autowired
    private KeycloakService keycloakService;

    @Test
    void testGetProfessorEmail_RealKeycloak() {
        String email = keycloakService.getProfessorEmail("53f7c620-d0e5-4efd-9022-efa8a0a3240d");

        // ASSERT
        assertNotNull(email);
        System.out.println("We successfully fetched real email from Keycloak: " + email);
    }
}
