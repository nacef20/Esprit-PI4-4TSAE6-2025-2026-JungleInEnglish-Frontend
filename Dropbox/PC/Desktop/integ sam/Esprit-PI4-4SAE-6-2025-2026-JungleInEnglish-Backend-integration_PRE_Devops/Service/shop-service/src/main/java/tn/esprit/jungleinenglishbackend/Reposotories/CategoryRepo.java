package tn.esprit.jungleinenglishbackend.Reposotories;

import tn.esprit.jungleinenglishbackend.Models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepo extends JpaRepository<Category, Integer> {
    Boolean existsByName(String name);

    Boolean existsByNameIgnoreCase(String name);

    java.util.Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findByIsActiveTrue();

    @Query("SELECT c FROM Category c WHERE c.isActive = true OR c.isActive IS NULL")
    List<Category> findByIsActiveTrueOrIsNull();
}
