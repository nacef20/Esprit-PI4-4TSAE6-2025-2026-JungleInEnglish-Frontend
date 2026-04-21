package tn.esprit.jungleinenglishbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.ProductService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductApiController (/api/products).
 */
@SpringBootTest(properties = {"app.security.mock-mode=true"})
@AutoConfigureMockMvc
class ProductApiControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ProductService productService;
    @MockitoBean ProductRepo productRepo;

    private Product mockProduct(int id, String title) {
        Product p = new Product();
        p.setId(id);
        p.setTitle(title);
        p.setCategory("Grammar");
        p.setPrice(15.0);
        p.setDiscountPrice(13.5);
        p.setDiscount(10);
        p.setStock(20);
        p.setIsActive(true);
        return p;
    }

    // ── GET /api/products ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/products — returns paginated product list")
    void getAll_ReturnsPaginatedList() throws Exception {
        var page = new PageImpl<>(List.of(mockProduct(1, "Grammar Book")),
                PageRequest.of(0, 10), 1);
        when(productService.getAllProductsPagination(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Grammar Book"));
    }

    @Test
    @DisplayName("GET /api/products?search=grammar — returns search results")
    void getAll_WithSearch_ReturnsSearchResult() throws Exception {
        var page = new PageImpl<>(List.of(mockProduct(1, "Grammar Mastery")),
                PageRequest.of(0, 10), 1);
        when(productService.searchProductPagination(0, 10, "grammar")).thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("search", "grammar")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Grammar Mastery"));
    }

    // ── GET /api/products/active ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/products/active — returns active products paginated")
    void getActive_ReturnsPaginatedActiveProducts() throws Exception {
        var page = new PageImpl<>(List.of(mockProduct(1, "Short Stories")),
                PageRequest.of(0, 12), 1);
        when(productService.getAllActiveProductPagination(0, 12, "")).thenReturn(page);

        mockMvc.perform(get("/api/products/active")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Short Stories"));
    }

    @Test
    @DisplayName("GET /api/products/active?category=Grammar — filters by category")
    void getActive_WithCategory_FiltersResults() throws Exception {
        var page = new PageImpl<>(List.of(mockProduct(1, "Grammar Book")),
                PageRequest.of(0, 12), 1);
        when(productService.getAllActiveProductPagination(0, 12, "Grammar")).thenReturn(page);

        mockMvc.perform(get("/api/products/active")
                        .param("category", "Grammar")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isOk());
    }

    // ── GET /api/products/{id} ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/products/{id} — existing → returns 200")
    void getById_Found_Returns200() throws Exception {
        when(productService.getProductById(1)).thenReturn(mockProduct(1, "Short Stories"));

        mockMvc.perform(get("/api/products/1")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Short Stories"));
    }

    @Test
    @DisplayName("GET /api/products/{id} — not found → returns 404")
    void getById_NotFound_Returns404() throws Exception {
        when(productService.getProductById(99)).thenReturn(null);

        mockMvc.perform(get("/api/products/99")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/products/{id} ──────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/products/{id} — existing → returns 204")
    void delete_Existing_Returns204() throws Exception {
        when(productRepo.existsById(1)).thenReturn(true);
        when(productService.deleteProduct(1)).thenReturn(true);

        mockMvc.perform(delete("/api/products/1")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} — not found → returns 404")
    void delete_NotFound_Returns404() throws Exception {
        when(productRepo.existsById(99)).thenReturn(false);

        mockMvc.perform(delete("/api/products/99")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isNotFound());
    }
}
