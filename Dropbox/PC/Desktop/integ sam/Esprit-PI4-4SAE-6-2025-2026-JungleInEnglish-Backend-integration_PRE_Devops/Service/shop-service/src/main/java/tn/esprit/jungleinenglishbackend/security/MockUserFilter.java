package tn.esprit.jungleinenglishbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Development-only filter that creates a mock JWT authentication
 * when the X-Mock-User-Id header is present.
 * <p>
 * Activated ONLY when app.security.mock-mode=true.
 * In production with Keycloak, this filter is not registered.
 */
public class MockUserFilter extends OncePerRequestFilter {

    private static final String MOCK_USER_HEADER = "X-Mock-User-Id";
    private static final String MOCK_EMAIL_HEADER = "X-Mock-User-Email";
    private static final String MOCK_ROLE_HEADER = "X-Mock-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String mockUserId = request.getHeader(MOCK_USER_HEADER);

        if (mockUserId != null && !mockUserId.isBlank()
                && (SecurityContextHolder.getContext().getAuthentication() == null
                        || SecurityContextHolder.getContext().getAuthentication() instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {

            String mockEmail = request.getHeader(MOCK_EMAIL_HEADER);
            if (mockEmail == null)
                mockEmail = "mock@example.com";

            String mockRole = request.getHeader(MOCK_ROLE_HEADER);
            if (mockRole == null)
                mockRole = "ROLE_USER";

            // Build a synthetic JWT for the mock user
            Jwt jwt = new Jwt(
                    "mock-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "none"),
                    Map.of(
                            "sub", mockUserId,
                            "email", mockEmail,
                            "preferred_username", "mock-user",
                            "realm_access", Map.of("roles", List.of(
                                    mockRole.replace("ROLE_", "").toLowerCase()))));

            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(mockRole));

            JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
