package tn.esprit.jungleinenglishbackend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * Holds user information extracted from JWT claims.
 * Used throughout the app instead of directly handling Jwt or SecurityContext.
 */
@Getter
@AllArgsConstructor
public class AuthenticatedUser {

    private final String userId;
    private final String email;
    private final String name;
    private final Set<String> roles;
}
