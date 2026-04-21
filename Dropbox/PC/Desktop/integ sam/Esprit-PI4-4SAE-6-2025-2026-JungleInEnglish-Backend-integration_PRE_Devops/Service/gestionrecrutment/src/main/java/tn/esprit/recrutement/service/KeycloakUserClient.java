package tn.esprit.recrutement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * HTTP client that delegates user management operations to the user-service,
 * which in turn talks to Keycloak.
 */
@Service
public class KeycloakUserClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url:http://localhost:2020}")
    private String userServiceUrl;

    public KeycloakUserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> getUserById(String keycloakId) {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userServiceUrl + "/api/users/" + keycloakId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public List<Map<String, Object>> getAllUsers() {
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                userServiceUrl + "/api/users",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

}
