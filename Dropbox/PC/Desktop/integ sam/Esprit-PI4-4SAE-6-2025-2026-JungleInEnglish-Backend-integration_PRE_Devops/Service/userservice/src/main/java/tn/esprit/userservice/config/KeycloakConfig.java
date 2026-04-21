package tn.esprit.userservice.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl("http://localhost:8085")
                .realm("jungleinenglish-realm")
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId("jungleinenglish-backend")
                .clientSecret("C61AAGtJ1a1KMUkRW22nIcRJqA9Xe2KL")
                .build();
    }
}
