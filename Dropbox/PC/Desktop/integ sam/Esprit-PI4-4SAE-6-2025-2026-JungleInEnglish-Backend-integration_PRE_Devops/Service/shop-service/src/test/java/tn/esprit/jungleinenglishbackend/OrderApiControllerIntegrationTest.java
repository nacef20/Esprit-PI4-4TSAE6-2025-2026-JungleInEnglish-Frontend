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
import tn.esprit.jungleinenglishbackend.Models.*;
import tn.esprit.jungleinenglishbackend.Services.OrderService;
import tn.esprit.jungleinenglishbackend.exceptions.InvalidLocationException;
import tn.esprit.jungleinenglishbackend.exceptions.OrderNotFoundException;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OrderApiController (/api/orders).
 */
@SpringBootTest(properties = {"app.security.mock-mode=true"})
@AutoConfigureMockMvc
class OrderApiControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean OrderService orderService;

    private ProductOrder mockOrder(int id) {
        ProductOrder o = new ProductOrder();
        o.setId(id);
        o.setOrderId("ORD-" + id);
        o.setUserId("user1");
        o.setStatus("IN_PROGRESS");
        return o;
    }

    private OrderDTO validOrderDTO() {
        OrderDTO dto = new OrderDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setTelephone("12345678"); // exactly 8 digits — satisfies @Pattern(regexp="\\d{8}")
        dto.setAddress("123 Main St");
        dto.setCity("Tunis");
        dto.setState("Tunis");
        dto.setPostalCode("1001"); // exactly 4 digits — satisfies @Pattern(regexp="\\d{4}")
        dto.setPaymentType("COD");
        return dto;
    }

    // ── GET /api/orders ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/orders — returns paginated orders")
    void getAll_ReturnsPaginated() throws Exception {
        var page = new PageImpl<>(List.of(mockOrder(1)), PageRequest.of(0, 10), 1);
        when(orderService.getAllOrdersPagination(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/orders")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].orderId").value("ORD-1"));
    }

    // ── GET /api/orders/my-orders ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/orders/my-orders — returns orders for current user")
    void getMyOrders_ReturnsUserOrders() throws Exception {
        when(orderService.getOrdersByUser(anyString())).thenReturn(List.of(mockOrder(1)));

        mockMvc.perform(get("/api/orders/my-orders")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ── GET /api/orders/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/orders/{id} — existing → returns 200")
    void getById_Found_Returns200() throws Exception {
        when(orderService.getOrderById(1)).thenReturn(mockOrder(1));

        mockMvc.perform(get("/api/orders/1")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-1"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} — not found → returns 404")
    void getById_NotFound_Returns404() throws Exception {
        when(orderService.getOrderById(99)).thenReturn(null);

        mockMvc.perform(get("/api/orders/99")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/orders ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/orders — valid payload → returns 201")
    void placeOrder_Valid_Returns201() throws Exception {
        doNothing().when(orderService).saveOrder(anyString(), org.mockito.ArgumentMatchers.any(OrderDTO.class));

        mockMvc.perform(post("/api/orders")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderDTO())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Order placed successfully"));
    }

    @Test
    @DisplayName("POST /api/orders — service throws → returns 500")
    void placeOrder_ServiceThrows_Returns500() throws Exception {
        doThrow(new RuntimeException("Cart is empty")).when(orderService)
                .saveOrder(anyString(), org.mockito.ArgumentMatchers.any(OrderDTO.class));

        mockMvc.perform(post("/api/orders")
                        .header("X-Mock-User-Id", "user1")
                        .header("X-Mock-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderDTO())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Cart is empty"));
    }

    // ── PUT /api/orders/{id}/status ────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/orders/{id}/status — valid status → returns 200")
    void updateStatus_Valid_Returns200() throws Exception {
        ProductOrder updated = mockOrder(1);
        updated.setStatus("Delivered");
        when(orderService.updateOrderStatus(eq(1), anyString())).thenReturn(updated);

        mockMvc.perform(put("/api/orders/1/status")
                        .param("st", "3")   // ID 3 = Delivered in OrderStatus enum
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Delivered"));
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status — invalid status id → returns 400")
    void updateStatus_InvalidStatusId_Returns400() throws Exception {
        mockMvc.perform(put("/api/orders/1/status")
                        .param("st", "999")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status id"));
    }

    // ── DELETE /api/orders/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/orders/{id} — returns 200 with message")
    void deleteOrder_Returns200() throws Exception {
        doNothing().when(orderService).deleteOrder(1);

        mockMvc.perform(delete("/api/orders/1")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order deleted successfully"));
    }

    // ── GET /api/orders/statuses ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/orders/statuses — returns all order statuses")
    void getStatuses_ReturnsAll() throws Exception {
        mockMvc.perform(get("/api/orders/statuses")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── PUT /api/orders/{id}/location ──────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/orders/{id}/location — valid coords → returns 200")
    void updateLocation_Valid_Returns200() throws Exception {
        doNothing().when(orderService).updateOrderLocation(eq(1), anyDouble(), anyDouble());

        mockMvc.perform(put("/api/orders/1/location")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\": 36.8, \"longitude\": 10.18}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Location updated successfully"));
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/location — invalid coords → returns 400")
    void updateLocation_Invalid_Returns400() throws Exception {
        doThrow(new InvalidLocationException("Invalid latitude: 999.0"))
                .when(orderService).updateOrderLocation(eq(1), anyDouble(), anyDouble());

        mockMvc.perform(put("/api/orders/1/location")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\": 999.0, \"longitude\": 10.18}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/location — order not found → returns 404")
    void updateLocation_OrderNotFound_Returns404() throws Exception {
        doThrow(new OrderNotFoundException("Order not found with id: 99"))
                .when(orderService).updateOrderLocation(eq(99), anyDouble(), anyDouble());

        mockMvc.perform(put("/api/orders/99/location")
                        .header("X-Mock-User-Id", "admin1")
                        .header("X-Mock-User-Role", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\": 36.8, \"longitude\": 10.18}"))
                .andExpect(status().isNotFound());
    }
}
