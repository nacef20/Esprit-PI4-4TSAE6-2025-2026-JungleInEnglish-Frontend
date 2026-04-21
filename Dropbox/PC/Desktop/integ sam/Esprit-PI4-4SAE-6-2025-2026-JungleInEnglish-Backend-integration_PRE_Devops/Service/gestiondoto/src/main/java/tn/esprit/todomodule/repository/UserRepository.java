package tn.esprit.todomodule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.todomodule.entity.Level;
import tn.esprit.todomodule.entity.Role;
import tn.esprit.todomodule.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    List<User> findAllByRolesContains(Role role);
    List<User> findAllByLevel(Level level);
}
