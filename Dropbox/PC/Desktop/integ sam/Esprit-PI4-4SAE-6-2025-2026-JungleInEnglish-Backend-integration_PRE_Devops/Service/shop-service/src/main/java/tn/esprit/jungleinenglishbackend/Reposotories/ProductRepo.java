package tn.esprit.jungleinenglishbackend.Reposotories;
import org.springframework.data.repository.query.Param;

import tn.esprit.jungleinenglishbackend.Models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Integer> {
    List<Product> findByIsActiveTrue();

    // Duplicate detection — case-insensitive
    boolean existsByTitleIgnoreCaseAndCategoryIgnoreCase(String title, String category);

    java.util.Optional<Product> findByTitleIgnoreCaseAndCategoryIgnoreCase(String title, String category);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE p.isActive = true OR p.isActive IS NULL")
    List<Product> findByIsActiveTrueOrIsActiveIsNull();

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE p.isActive = true OR p.isActive IS NULL")
    Page<Product> findByIsActiveTrueOrIsActiveIsNull(Pageable pageable);

    List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2);

    Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE (p.isActive = true OR p.isActive IS NULL) AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :ch, '%')) OR LOWER(p.category) LIKE LOWER(CONCAT('%', :ch2, '%')))")
    Page<Product> findByIsActiveTrueOrIsActiveIsNullAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            @Param("ch") String ch,
            @Param("ch2") String ch2,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE (p.isActive = true OR p.isActive IS NULL) AND p.category = :category")
    Page<Product> findByIsActiveTrueOrIsActiveIsNullAndCategory(@Param("category") String category, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE (p.isActive = true OR p.isActive IS NULL) AND p.category = :category AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :ch, '%')) OR LOWER(p.category) LIKE LOWER(CONCAT('%', :ch2, '%')))")
    Page<Product> findByIsActiveTrueOrIsActiveIsNullAndCategoryAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            @Param("category") String category,
            @Param("ch") String ch,
            @Param("ch2") String ch2,
            Pageable pageable);
}
