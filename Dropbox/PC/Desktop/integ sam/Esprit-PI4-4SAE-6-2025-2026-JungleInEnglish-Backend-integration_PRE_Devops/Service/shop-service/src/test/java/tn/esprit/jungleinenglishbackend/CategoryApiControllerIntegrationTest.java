package tn.esprit.jungleinenglishbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.jungleinenglishbackend.Models.Category;
import tn.esprit.jungleinenglishbackend.Reposotories.CategoryRepo;
import tn.esprit.jungleinenglishbackend.Services.CategoryService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CategoryApiController (/api/categories).
 */
@SpringBootTest(properties = {"app.security.mock-mode=true"})
@AutoConfigureMockMvc
class CategoryApiControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CategoryService categoryService;
    @MockitoBean CategoryRepo categoryRepo;

    private static final String ADMIN = "ROLE_ADMIN";
    private static final String USER  = "ROLE_USER";

    private Category mockCategory(int id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setIsActive(true);
        c.setImageName("default.jpg");
        return c;
    }

    // ── GET /api/categories ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/categories — returns paginated list")
    void getAll_ReturnsPaginatedList() throws Exception {
        Page<Category> page = new PageImpl<>(List.of(mockCategory(1, "Grammar")),
                PageRequest.of(0, 10), 1);
        when(categoryService.getAllCategorPagination(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/categories")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Grammar"));
    }

    // ── GET /api/categories/active ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/categories/active — returns active categories")
    void getActive_ReturnsActiveCategories() throws Exception {
        when(categoryService.getAllActiveCategory()).thenReturn(List.of(mockCategory(1, "Light Novel")));

        mockMvc.perform(get("/api/categories/active")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Light Novel"));
    }

    // ── GET /api/categories/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/categories/{id} — existing → returns 200 with category")
    void getById_Found_Returns200() throws Exception {
        when(categoryService.getCategoryById(1)).thenReturn(mockCategory(1, "Grammar"));

        mockMvc.perform(get("/api/categories/1")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Grammar"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} — not found → returns 404")
    void getById_NotFound_Returns404() throws Exception {
        when(categoryService.getCategoryById(99)).thenReturn(null);

        mockMvc.perform(get("/api/categories/99")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", USER))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/categories ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/categories — valid body → returns 201")
    void create_Valid_Returns201() throws Exception {
        Category cat = mockCategory(0, "Comics");
        when(categoryService.existCategory("Comics")).thenReturn(false);
        when(categoryService.saveCategory(any())).thenReturn(mockCategory(2, "Comics"));

        mockMvc.perform(post("/api/categories")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Comics"));
    }

    @Test
    @DisplayName("POST /api/categories — blank name → returns 400")
    void create_BlankName_Returns400() throws Exception {
        Category cat = new Category();
        cat.setName("   ");

        mockMvc.perform(post("/api/categories")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/categories — duplicate name → returns 409")
    void create_DuplicateName_Returns409() throws Exception {
        Category cat = mockCategory(0, "Grammar");
        when(categoryService.existCategory("Grammar")).thenReturn(true);

        mockMvc.perform(post("/api/categories")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isConflict());
    }

    // ── DELETE /api/categories/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/categories/{id} — existing → returns 204")
    void delete_Existing_Returns204() throws Exception {
        when(categoryRepo.existsById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/categories/1")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", ADMIN))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} — not found → returns 404")
    void delete_NotFound_Returns404() throws Exception {
        when(categoryRepo.existsById(99)).thenReturn(false);

        mockMvc.perform(delete("/api/categories/99")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", ADMIN))
                .andExpect(status().isNotFound());
    }
}
