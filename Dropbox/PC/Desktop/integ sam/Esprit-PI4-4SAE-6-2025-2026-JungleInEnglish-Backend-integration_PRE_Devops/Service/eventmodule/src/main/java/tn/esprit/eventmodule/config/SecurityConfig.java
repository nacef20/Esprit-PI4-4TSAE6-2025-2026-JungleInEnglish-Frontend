package tn.esprit.eventmodule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/actuator/**"
                ).permitAll()

                // Event management – ADMIN only
                .requestMatchers(HttpMethod.POST,   "/api/evenements/add").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/evenements/update").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/evenements/delete").hasRole("ADMIN")

                // Event read – ADMIN + PARTICIPANT
                .requestMatchers(HttpMethod.GET, "/api/evenements/**").hasAnyRole("ADMIN", "PARTICIPANT")

                // Participation – PARTICIPANT (register, unregister, checkin, scan)
                .requestMatchers("/api/participations/register").hasRole("PARTICIPANT")
                .requestMatchers("/api/participations/unregister").hasRole("PARTICIPANT")
                .requestMatchers("/api/participations/checkin").hasAnyRole("ADMIN", "PARTICIPANT")
                .requestMatchers("/api/participations/scan").hasAnyRole("ADMIN", "PARTICIPANT")
                .requestMatchers(HttpMethod.GET, "/api/participations/**").hasAnyRole("ADMIN", "PARTICIPANT")

                // Feedback – PARTICIPANT can add, both can read
                .requestMatchers(HttpMethod.POST, "/api/event-feedbacks/add").hasRole("PARTICIPANT")
                .requestMatchers(HttpMethod.GET, "/api/event-feedbacks/**").hasAnyRole("ADMIN", "PARTICIPANT")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
            return authorities;
        });
        return converter;
    }
}
