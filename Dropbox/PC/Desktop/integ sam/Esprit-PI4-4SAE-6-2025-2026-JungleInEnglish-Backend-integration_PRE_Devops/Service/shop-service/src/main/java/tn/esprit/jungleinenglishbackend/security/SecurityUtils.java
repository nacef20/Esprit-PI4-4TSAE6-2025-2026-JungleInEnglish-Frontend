package tn.esprit.jungleinenglishbackend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static utility for extracting the current user's identity from the
 * SecurityContext.
 * Works with both real JWT (Keycloak) and mock authentication.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Get the current authenticated user's ID (Keycloak 'sub' claim).
     */
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getSubject();
        }
        // Fallback: principal name
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getName();
        }
        throw new IllegalStateException("No authenticated user found in SecurityContext");
    }

    /**
     * Get full user info from the JWT claims.
     */
    public static AuthenticatedUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String userId = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("preferred_username");

            Set<String> roles = extractRoles(jwt);

            return new AuthenticatedUser(userId, email, name, roles);
        }
        // Mock mode fallback
        if (auth != null) {
            return new AuthenticatedUser(
                    auth.getName(),
                    "mock@example.com",
                    "Mock User",
                    Collections.singleton("ROLE_USER"));
        }
        throw new IllegalStateException("No authenticated user found in SecurityContext");
    }

    @SuppressWarnings("unchecked")
    private static Set<String> extractRoles(Jwt jwt) {
        Set<String> roles = new HashSet<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                realmRoles.forEach(r -> roles.add("ROLE_" + r.toUpperCase()));
            }
        }
        return roles;
    }
}
