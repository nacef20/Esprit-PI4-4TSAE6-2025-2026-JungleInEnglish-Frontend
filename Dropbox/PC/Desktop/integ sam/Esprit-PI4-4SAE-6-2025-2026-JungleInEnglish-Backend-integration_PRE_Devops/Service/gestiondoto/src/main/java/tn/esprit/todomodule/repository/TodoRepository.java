package tn.esprit.todomodule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.todomodule.entity.Level;
import tn.esprit.todomodule.entity.Todo;
import tn.esprit.todomodule.entity.TodoStatus;
import tn.esprit.todomodule.entity.User;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByStatus(TodoStatus status);
    List<Todo> findByLevel(Level level);
    List<Todo> findByUser(User user);
    List<Todo> findByLevelOrUser(Level level, User user);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Todo t WHERE (t.level = :level AND t.level IS NOT NULL) OR t.user.id = :userId")
    List<Todo> findByLevelOrUserId(@org.springframework.data.repository.query.Param("level") Level level, @org.springframework.data.repository.query.Param("userId") String userId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Todo t WHERE t.level IS NOT NULL OR t.user.id = :userId")
    List<Todo> findByLevelIsNotNullOrUserId(@org.springframework.data.repository.query.Param("userId") String userId);

    void deleteByDueDateBefore(java.time.LocalDateTime now);
}
