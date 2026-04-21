package tn.esprit.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Configuration

@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .pathMatchers("/public/**").permitAll()
                        .pathMatchers("/uploads/**").permitAll()
                        .pathMatchers("/img/**").permitAll()
                        .pathMatchers("/api/users/**").permitAll()
                        .pathMatchers("/ws/**").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/ws/info/**").permitAll()
                        .pathMatchers("/chatPrivee/**").hasRole("USER")
                        .pathMatchers("/messageGroup/**").hasRole("USER")
                        .pathMatchers("/joinGroup/**").hasRole("USER")
                        .pathMatchers("/membreGroupChat/**").hasRole("USER")
                        .pathMatchers("posts/**").authenticated()

                        // Event module – event management (ADMIN only)
                        .pathMatchers(HttpMethod.POST, "/api/evenements/add").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/evenements/update").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/evenements/delete").hasRole("ADMIN")
                        // Event module – read events (ADMIN + PARTICIPANT)
                        .pathMatchers(HttpMethod.GET, "/api/evenements/**").hasAnyRole("ADMIN", "PARTICIPANT")
                        // Event module – participation
                        .pathMatchers("/api/participations/register").hasRole("PARTICIPANT")
                        .pathMatchers("/api/participations/unregister").hasRole("PARTICIPANT")
                        .pathMatchers("/api/participations/checkin").hasAnyRole("ADMIN", "PARTICIPANT")
                        .pathMatchers("/api/participations/scan").hasAnyRole("ADMIN", "PARTICIPANT")
                        .pathMatchers(HttpMethod.GET, "/api/participations/**").hasAnyRole("ADMIN", "PARTICIPANT")
                        // Event module – feedback
                        .pathMatchers(HttpMethod.POST, "/api/event-feedbacks/add").hasRole("PARTICIPANT")
                        .pathMatchers(HttpMethod.GET, "/api/event-feedbacks/**").hasAnyRole("ADMIN", "PARTICIPANT")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakConverter()))
                )
                .build();
    }



    @Bean
    public ReactiveJwtAuthenticationConverter keycloakConverter() {

        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            System.out.println("=== JWT DEBUG ===");
            System.out.println("realmAccess = " + realmAccess);

            if (realmAccess == null || realmAccess.get("roles") == null) {
                System.out.println("NO ROLES FOUND");
                return Flux.empty();
            }

            List<String> roles = (List<String>) realmAccess.get("roles");

            System.out.println("ROLES FROM KEYCLOAK = " + roles);

            System.out.println("=== END JWT DEBUG ===");

            return Flux.fromIterable(
                    roles.stream()
                            // 🔥 filtre les rôles système Keycloak
                            .filter(role -> role != null && !role.startsWith("default-roles"))
                            .map(role -> {
                                String authority = "ROLE_" + role;
                                System.out.println("ADDING AUTHORITY = " + authority);
                                return (GrantedAuthority) new SimpleGrantedAuthority(authority);
                            })
                            .toList()
            );
        });

        return converter;
    }

}
