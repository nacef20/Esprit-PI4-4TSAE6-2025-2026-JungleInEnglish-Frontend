package tn.esprit.jungleinenglishbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.jungleinenglishbackend.Models.Cart;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Reposotories.CartRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.CartService;
import tn.esprit.jungleinenglishbackend.Services.ProductService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CartApiController (/api/cart).
 */
@SpringBootTest(properties = {"app.security.mock-mode=true"})
@AutoConfigureMockMvc
class CartApiControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CartService cartService;
    @MockitoBean CartRepo cartRepo;
    @MockitoBean ProductRepo productRepo;
    @MockitoBean ProductService productService;

    private Product mockProduct(int id) {
        Product p = new Product();
        p.setId(id);
        p.setTitle("Test Product");
        p.setPrice(10.0);
        p.setDiscountPrice(9.0);
        return p;
    }

    private Cart mockCart(int id, String userId) {
        Cart c = new Cart();
        c.setId(id);
        c.setUserId(userId);
        c.setProduct(mockProduct(1));
        c.setQuantity(2);
        c.setTotalPrice(18.0);
        return c;
    }

    // ── GET /api/cart ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/cart — returns cart items for current user")
    void getCart_ReturnsItems() throws Exception {
        when(cartService.getCartsByUser(anyString()))
                .thenReturn(List.of(mockCart(1, "user1")));

        mockMvc.perform(get("/api/cart")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    // ── GET /api/cart/count ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/cart/count — returns count of items")
    void getCartCount_ReturnsCount() throws Exception {
        when(cartService.getCountCart(anyString())).thenReturn(3);

        mockMvc.perform(get("/api/cart/count")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    // ── POST /api/cart ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/cart — valid productId → returns 201 with cart item")
    void addToCart_Valid_Returns201() throws Exception {
        when(cartService.saveCart(eq(1), anyString())).thenReturn(mockCart(1, "user1"));

        mockMvc.perform(post("/api/cart")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    @DisplayName("POST /api/cart — missing productId → returns 400")
    void addToCart_MissingProductId_Returns400() throws Exception {
        mockMvc.perform(post("/api/cart")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("productId is required"));
    }

    // ── PUT /api/cart/{id}/quantity ────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/cart/{id}/quantity?sy=in — increments quantity → returns 200")
    void updateQuantity_Increment_Returns200() throws Exception {
        doNothing().when(cartService).updateQuantity("in", 1);

        mockMvc.perform(put("/api/cart/1/quantity")
                        .param("sy", "in")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Quantity updated"));
    }

    @Test
    @DisplayName("PUT /api/cart/{id}/quantity?sy=de — decrements quantity → returns 200")
    void updateQuantity_Decrement_Returns200() throws Exception {
        doNothing().when(cartService).updateQuantity("de", 1);

        mockMvc.perform(put("/api/cart/1/quantity")
                        .param("sy", "de")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/cart/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/cart/{id} — existing → returns 204")
    void remove_Existing_Returns204() throws Exception {
        when(cartRepo.existsById(1)).thenReturn(true);
        doNothing().when(cartRepo).deleteById(1);

        mockMvc.perform(delete("/api/cart/1")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/cart/{id} — not found → returns 404")
    void remove_NotFound_Returns404() throws Exception {
        when(cartRepo.existsById(99)).thenReturn(false);

        mockMvc.perform(delete("/api/cart/99")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isNotFound());
    }
}
