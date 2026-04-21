package tn.esprit.jungleinenglishbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tn.esprit.jungleinenglishbackend.security.MockUserFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.mock-mode:false}")
    private boolean mockMode;

    @Autowired
    private JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (mockMode) {
            // ── DEV / MOCK MODE ──────────────────────────────────────────────────────
            // MockUserFilter sets identity from X-Mock-User-* headers.
            // All endpoints are open so role-based 403s cannot occur during development.
            http.addFilterBefore(new MockUserFilter(), UsernamePasswordAuthenticationFilter.class);
            http.authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll());
        } else {
            // ── PRODUCTION MODE ──────────────────────────────────────────────────────
            http.authorizeHttpRequests(auth -> auth
                    // Public – read-only catalog
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .requestMatchers("/api/orders/statuses").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/orders/*/location").permitAll()
                    // Product & category management – admin only
                    .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                    // Admin dashboard & inventory
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/inventory/**").hasRole("ADMIN")
                    // Static resources
                    .requestMatchers("/img/**").permitAll()
                    // Everything else requires authentication
                    .anyRequest().authenticated());

            // Validate JWT issued by Keycloak
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
        }

        return http.build();
    }
}
