package tn.esprit.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.userservice.service.KeycloakUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final KeycloakUserService userService;

    public UserController(KeycloakUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Object getUser(@PathVariable String id) {
        var user = userService.getUserById(id);

        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail()
        );
    }
    @GetMapping("/me")
    public Object getCurrentUser(@AuthenticationPrincipal Jwt jwt) {

        return Map.of(
                "id", jwt.getSubject(),
                "username", jwt.getClaimAsString("preferred_username"),
                "firstName", jwt.getClaimAsString("given_name"),
                "lastName", jwt.getClaimAsString("family_name"),
                "email", jwt.getClaimAsString("email"),
                "roles", jwt.getClaimAsMap("realm_access").get("roles")
        );
    }
}
