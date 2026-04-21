package tn.esprit.jungleinenglishbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tn.esprit.jungleinenglishbackend.Models.Category;
import tn.esprit.jungleinenglishbackend.Reposotories.CategoryRepo;
import tn.esprit.jungleinenglishbackend.Services.impl.CategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepo categoryRepo;
    @InjectMocks private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1);
        category.setName("Light Novel");
        category.setIsActive(true);
    }

    // ── saveCategory ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveCategory — delegates to repo and returns saved entity")
    void saveCategory_ReturnsSaved() {
        when(categoryRepo.save(category)).thenReturn(category);
        Category result = categoryService.saveCategory(category);
        assertThat(result.getName()).isEqualTo("Light Novel");
        verify(categoryRepo).save(category);
    }

    // ── getAllCategory ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCategory — returns all categories from repo")
    void getAllCategory_ReturnsAll() {
        when(categoryRepo.findAll()).thenReturn(List.of(category));
        List<Category> result = categoryService.getAllCategory();
        assertThat(result).hasSize(1);
    }

    // ── existCategory ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("existCategory — returns true when name exists")
    void existCategory_True() {
        when(categoryRepo.existsByNameIgnoreCase("Light Novel")).thenReturn(true);
        assertThat(categoryService.existCategory("Light Novel")).isTrue();
    }

    @Test
    @DisplayName("existCategory — returns false when name does not exist")
    void existCategory_False() {
        when(categoryRepo.existsByNameIgnoreCase("Comics")).thenReturn(false);
        assertThat(categoryService.existCategory("Comics")).isFalse();
    }

    // ── deleteCategory ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCategory — existing ID → deletes and returns true")
    void deleteCategory_Existing_ReturnsTrue() {
        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        Boolean result = categoryService.deleteCategory(1);
        assertThat(result).isTrue();
        verify(categoryRepo).delete(category);
    }

    @Test
    @DisplayName("deleteCategory — non-existing ID → returns false without deleting")
    void deleteCategory_NonExisting_ReturnsFalse() {
        when(categoryRepo.findById(99)).thenReturn(Optional.empty());
        Boolean result = categoryService.deleteCategory(99);
        assertThat(result).isFalse();
        verify(categoryRepo, never()).delete(any());
    }

    // ── getCategoryById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getCategoryById — existing ID → returns category")
    void getCategoryById_Found() {
        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));
        Category result = categoryService.getCategoryById(1);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Light Novel");
    }

    @Test
    @DisplayName("getCategoryById — non-existing ID → returns null")
    void getCategoryById_NotFound() {
        when(categoryRepo.findById(99)).thenReturn(Optional.empty());
        Category result = categoryService.getCategoryById(99);
        assertThat(result).isNull();
    }

    // ── getAllActiveCategory ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAllActiveCategory — returns only active categories")
    void getAllActiveCategory_ReturnsActive() {
        when(categoryRepo.findByIsActiveTrueOrIsNull()).thenReturn(List.of(category));
        List<Category> result = categoryService.getAllActiveCategory();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
    }

    // ── getAllCategorPagination ────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCategorPagination — returns paginated categories")
    void getAllCategorPagination_ReturnsPage() {
        Page<Category> page = new PageImpl<>(List.of(category));
        when(categoryRepo.findAll(PageRequest.of(0, 5))).thenReturn(page);
        Page<Category> result = categoryService.getAllCategorPagination(0, 5);
        assertThat(result.getContent()).hasSize(1);
    }
}
