package com.fluently.feedbackservice.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.springframework.context.annotation.Import;
import com.fluently.feedbackservice.config.RestTemplateConfig;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;


@RestClientTest(components = KeycloakService.class)
@Import(RestTemplateConfig.class)
@TestPropertySource(properties = {
    "keycloak.auth-server-url=http://keycloak.mock",
    "keycloak.realm=test-realm",
    "keycloak.client-id=test-client",
    "keycloak.client-secret=test-secret"
})
public class KeycloakServiceIntegrationTest {

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // We bind the mock server to our custom RestTemplate
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    @DisplayName("Should successfully fetch Professor Email from external service interaction")
    void testGetProfessorEmail_Interaction() {
        // 1. Arrange the Mock Server to expect interactions
        mockServer.expect(requestTo("http://keycloak.mock/realms/test-realm/protocol/openid-connect/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\": \"mocked-token-123\"}", 
                        MediaType.APPLICATION_JSON
                ));

        // Mocking the user fetch interaction
        mockServer.expect(requestTo("http://keycloak.mock/admin/realms/test-realm/users/prof-123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"email\": \"professor@jungle.com\"}", 
                        MediaType.APPLICATION_JSON
                ));

        // 2. Act
        String email = keycloakService.getProfessorEmail("prof-123");

        // 3. Assert
        assertEquals("professor@jungle.com", email);
        
        // 4. Verify all interactions that we expected actually happened!
        mockServer.verify(); 
    }
}
