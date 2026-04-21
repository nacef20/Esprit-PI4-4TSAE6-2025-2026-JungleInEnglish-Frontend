package tn.esprit.jungleinenglishbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.jungleinenglishbackend.Models.*;
import tn.esprit.jungleinenglishbackend.Reposotories.CartRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductOrderRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.impl.OrderServiceImpl;
import tn.esprit.jungleinenglishbackend.exceptions.InsufficientStockException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private ProductOrderRepo orderRepository;
    @Mock private CartRepo cartRepository;
    @Mock private ProductRepo productRepository;
    @InjectMocks private OrderServiceImpl orderService;

    private Product product;
    private Cart cart;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1);
        product.setTitle("English Grammar Guide");
        product.setPrice(15.0);
        product.setDiscountPrice(13.5);
        product.setStock(10);

        cart = new Cart();
        cart.setProduct(product);
        cart.setUserId("user1");
        cart.setQuantity(2);

        orderDTO = new OrderDTO();
        orderDTO.setFirstName("John");
        orderDTO.setLastName("Doe");
        orderDTO.setEmail("john@example.com");
        orderDTO.setTelephone("123456789");
        orderDTO.setAddress("123 Main St");
        orderDTO.setCity("Tunis");
        orderDTO.setState("Tunis");
        orderDTO.setPostalCode("1001");
        orderDTO.setPaymentType("COD");
    }

    // ── saveOrder ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveOrder — valid cart → creates orders and clears cart")
    void saveOrder_ValidCart_CreatesOrdersAndClearsCart() throws Exception {
        when(cartRepository.findByUserId("user1")).thenReturn(List.of(cart));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(ProductOrder.class))).thenAnswer(i -> i.getArgument(0));

        orderService.saveOrder("user1", orderDTO);

        verify(orderRepository).save(any(ProductOrder.class));
        verify(cartRepository).deleteByUserId("user1");
        // Stock must have been decremented: 10 - 2 = 8
        assertThat(product.getStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("saveOrder — empty cart → throws Exception")
    void saveOrder_EmptyCart_ThrowsException() {
        when(cartRepository.findByUserId("user1")).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.saveOrder("user1", orderDTO))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    @DisplayName("saveOrder — insufficient stock → throws InsufficientStockException")
    void saveOrder_InsufficientStock_ThrowsException() {
        product.setStock(1);
        cart.setQuantity(5); // requesting more than stock

        when(cartRepository.findByUserId("user1")).thenReturn(List.of(cart));

        assertThatThrownBy(() -> orderService.saveOrder("user1", orderDTO))
                .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any());
    }

    // ── getOrdersByUser ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrdersByUser — returns orders for the given user")
    void getOrdersByUser_ReturnsList() {
        ProductOrder order = new ProductOrder();
        order.setUserId("user1");
        when(orderRepository.findByUserId("user1")).thenReturn(List.of(order));

        List<ProductOrder> result = orderService.getOrdersByUser("user1");

        assertThat(result).hasSize(1);
    }

    // ── updateOrderStatus ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateOrderStatus — existing order → updates and returns order")
    void updateOrderStatus_ExistingOrder_UpdatesStatus() {
        ProductOrder order = new ProductOrder();
        order.setId(1);
        order.setStatus("IN_PROGRESS");

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProductOrder result = orderService.updateOrderStatus(1, "DELIVERED");

        assertThat(result.getStatus()).isEqualTo("DELIVERED");
    }

    @Test
    @DisplayName("updateOrderStatus — non-existing order → returns null")
    void updateOrderStatus_NonExistingOrder_ReturnsNull() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());
        ProductOrder result = orderService.updateOrderStatus(99, "DELIVERED");
        assertThat(result).isNull();
    }

    // ── getAllOrders ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllOrders — returns all orders from repository")
    void getAllOrders_ReturnsAll() {
        when(orderRepository.findAll()).thenReturn(List.of(new ProductOrder(), new ProductOrder()));
        assertThat(orderService.getAllOrders()).hasSize(2);
    }

    // ── deleteOrder ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteOrder — calls deleteById on repository")
    void deleteOrder_CallsDeleteById() {
        orderService.deleteOrder(5);
        verify(orderRepository).deleteById(5);
    }

    // ── getOrderById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrderById — existing ID → returns order")
    void getOrderById_Found() {
        ProductOrder order = new ProductOrder();
        order.setId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        assertThat(orderService.getOrderById(1)).isNotNull();
    }

    @Test
    @DisplayName("getOrderById — non-existing ID → returns null")
    void getOrderById_NotFound() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());
        assertThat(orderService.getOrderById(99)).isNull();
    }

    // ── updateOrderLocation validation ────────────────────────────────────────

    @Test
    @DisplayName("updateOrderLocation — invalid latitude → throws InvalidLocationException")
    void updateOrderLocation_InvalidLatitude_Throws() {
        assertThatThrownBy(() -> orderService.updateOrderLocation(1, 999.0, 10.0))
                .isInstanceOf(tn.esprit.jungleinenglishbackend.exceptions.InvalidLocationException.class)
                .hasMessageContaining("latitude");
    }

    @Test
    @DisplayName("updateOrderLocation — invalid longitude → throws InvalidLocationException")
    void updateOrderLocation_InvalidLongitude_Throws() {
        assertThatThrownBy(() -> orderService.updateOrderLocation(1, 36.0, 999.0))
                .isInstanceOf(tn.esprit.jungleinenglishbackend.exceptions.InvalidLocationException.class)
                .hasMessageContaining("longitude");
    }
}
