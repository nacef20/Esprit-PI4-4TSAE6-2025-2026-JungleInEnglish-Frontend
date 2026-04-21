package tn.esprit.todomodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import tn.esprit.todomodule.entity.Level;
import tn.esprit.todomodule.entity.Role;
import tn.esprit.todomodule.entity.User;
import tn.esprit.todomodule.repository.UserRepository;
import java.util.Set;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "tn.esprit.todomodule.repository")
@EnableScheduling
@org.springframework.cloud.client.discovery.EnableDiscoveryClient
public class TodomoduleApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodomoduleApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User user = new User();
                user.setId("1"); // Manual ID since @GeneratedValue was removed
                user.setUsername("default_user");
                user.setEmail("default@test.com");
                user.setPassword("password");
                user.setRoles(Set.of(Role.ROLE_STUDENT));
                user.setLevel(Level.A1);
                userRepository.save(user);
                System.out.println("Seeded default user: " + user.getUsername());
            }
        };
    }
}
