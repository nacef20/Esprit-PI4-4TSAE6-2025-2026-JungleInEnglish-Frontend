package tn.esprit.todomodule.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.esprit.todomodule.entity.Role;
import tn.esprit.todomodule.entity.User;
import tn.esprit.todomodule.repository.UserRepository;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserSyncFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public UserSyncFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String userId = jwtAuth.getToken().getClaimAsString("sub");
            
            if (userId != null && !userRepository.existsById(userId)) {
                syncUser(userId, jwtAuth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void syncUser(String userId, JwtAuthenticationToken jwtAuth) {
        User user = new User();
        user.setId(userId);
        
        // Extract info from JWT
        String username = jwtAuth.getToken().getClaimAsString("preferred_username");
        String email = jwtAuth.getToken().getClaimAsString("email");
        
        user.setUsername(username != null ? username : "user_" + userId.substring(0, 8));
        user.setEmail(email != null ? email : userId + "@noreply.com");
        user.setPassword("EXTERNAL_OIDC"); // Placeholder for required field
        
        // Map roles from Keycloak
        Set<Role> roles = new HashSet<>();
        List<String> realmRoles = jwtAuth.getToken().getClaimAsStringList("realm_access.roles");
        
        if (realmRoles != null) {
            if (realmRoles.stream().anyMatch(r -> r.equalsIgnoreCase("admin") || r.equalsIgnoreCase("teacher"))) {
                roles.add(Role.ROLE_TEACHER);
            } else {
                roles.add(Role.ROLE_STUDENT);
            }
        } else {
            roles.add(Role.ROLE_STUDENT);
        }
        
        user.setRoles(roles);
        userRepository.save(user);
        System.out.println("[UserSync] Automatically registered user: " + user.getUsername());
    }
}
