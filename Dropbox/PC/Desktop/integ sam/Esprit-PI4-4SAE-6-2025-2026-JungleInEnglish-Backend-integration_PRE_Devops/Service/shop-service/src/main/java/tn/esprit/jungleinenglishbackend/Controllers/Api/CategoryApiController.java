package tn.esprit.jungleinenglishbackend.Controllers.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.jungleinenglishbackend.Models.Category;
import tn.esprit.jungleinenglishbackend.Reposotories.CategoryRepo;
import tn.esprit.jungleinenglishbackend.Services.CategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryApiController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepo categoryRepo;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        Page<Category> page = categoryService.getAllCategorPagination(pageNo, pageSize);
        return ResponseEntity.ok(Map.of(
                "content", page.getContent(),
                "pageNo", page.getNumber(),
                "pageSize", page.getSize(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "first", page.isFirst(),
                "last", page.isLast()));
    }

    @GetMapping("/active")
    public List<Category> getActive() {
        return categoryService.getAllActiveCategory();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") int id) {
        Category cat = categoryService.getCategoryById(id);
        if (cat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", "Category not found"));
        }
        return ResponseEntity.ok(cat);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad Request", "message", "Category name is required"));
        }
        if (categoryService.existCategory(category.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflict", "message", "Category name already exists"));
        }
        if (category.getImageName() == null)
            category.setImageName("default.jpg");
        if (category.getIsActive() == null)
            category.setIsActive(true);
        Category saved = categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") int id, @RequestBody Category category) {
        Category existing = categoryService.getCategoryById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", "Category not found"));
        }
        if (category.getName() != null && !category.getName().equalsIgnoreCase(existing.getName())) {
            if (categoryService.existCategory(category.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Conflict", "message", "Category name already exists"));
            }
            existing.setName(category.getName());
        }
        if (category.getIsActive() != null)
            existing.setIsActive(category.getIsActive());
        if (category.getImageName() != null)
            existing.setImageName(category.getImageName());
        Category saved = categoryService.saveCategory(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") int id) {
        if (!categoryRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
