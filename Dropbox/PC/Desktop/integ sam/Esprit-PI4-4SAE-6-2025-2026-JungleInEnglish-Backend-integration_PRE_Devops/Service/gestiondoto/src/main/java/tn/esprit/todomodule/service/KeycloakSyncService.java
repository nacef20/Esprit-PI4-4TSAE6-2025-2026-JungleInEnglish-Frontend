package tn.esprit.todomodule.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.todomodule.entity.Role;
import tn.esprit.todomodule.entity.User;
import tn.esprit.todomodule.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class KeycloakSyncService {

    @Value("${keycloak.auth-server-url:http://localhost:8085}")
    private String serverUrl;

    @Value("${keycloak.realm:jungleinenglish-realm}")
    private String realm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:111}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String clientId;

    private final UserRepository userRepository;

    public KeycloakSyncService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void syncAllUsers() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // Connect via master for admin actions
                .clientId(clientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();

        try {
            List<UserRepresentation> users = keycloak.realm(realm).users().list();
            int count = 0;
            for (UserRepresentation kr : users) {
                if (!userRepository.existsById(kr.getId())) {
                    User user = new User();
                    user.setId(kr.getId());
                    user.setUsername(kr.getUsername());
                    user.setEmail(kr.getEmail() != null ? kr.getEmail() : kr.getUsername() + "@noreply.com");
                    user.setPassword("EXTERNAL_OIDC");
                    
                    // Default to Student, or Teacher if they have the role
                    Set<Role> roles = new HashSet<>();
                    roles.add(Role.ROLE_STUDENT);
                    
                    // Fetch roles for this user in the realm
                    List<String> roleNames = keycloak.realm(realm).users().get(kr.getId()).roles().realmLevel().listAll()
                            .stream().map(r -> r.getName().toLowerCase()).toList();
                    
                    if (roleNames.contains("teacher") || roleNames.contains("admin")) {
                        roles.add(Role.ROLE_TEACHER);
                    }
                    
                    user.setRoles(roles);
                    userRepository.save(user);
                    count++;
                }
            }
            System.out.println("[KeycloakSync] Successfully synced " + count + " new users from Keycloak.");
        } finally {
            keycloak.close();
        }
    }
}
