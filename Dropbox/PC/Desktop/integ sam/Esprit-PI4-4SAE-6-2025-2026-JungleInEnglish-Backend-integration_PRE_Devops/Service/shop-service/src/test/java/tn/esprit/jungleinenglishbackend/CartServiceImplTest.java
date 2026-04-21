package tn.esprit.jungleinenglishbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.jungleinenglishbackend.Models.Cart;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Reposotories.CartRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.impl.CartServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepo cartRepo;
    @Mock private ProductRepo productRepo;
    @InjectMocks private CartServiceImpl cartService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1);
        product.setTitle("Grammar Book");
        product.setPrice(10.0);
        product.setDiscountPrice(9.0);
    }

    // ── saveCart ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveCart — new product not yet in cart → creates entry with quantity 1")
    void saveCart_NewProduct_CreatesCartEntry() {
        when(productRepo.findById(1)).thenReturn(Optional.of(product));
        when(cartRepo.findByProductIdAndUserId(1, "user1")).thenReturn(null);

        Cart saved = new Cart();
        saved.setProduct(product);
        saved.setUserId("user1");
        saved.setQuantity(1);
        saved.setTotalPrice(9.0);
        when(cartRepo.save(any(Cart.class))).thenReturn(saved);

        Cart result = cartService.saveCart(1, "user1");

        assertThat(result.getQuantity()).isEqualTo(1);
        assertThat(result.getTotalPrice()).isEqualTo(9.0);
        verify(cartRepo).save(any(Cart.class));
    }

    @Test
    @DisplayName("saveCart — product already in cart → increments quantity")
    void saveCart_ExistingProduct_IncrementsQuantity() {
        Cart existing = new Cart();
        existing.setProduct(product);
        existing.setUserId("user1");
        existing.setQuantity(2);
        existing.setTotalPrice(18.0);

        when(productRepo.findById(1)).thenReturn(Optional.of(product));
        when(cartRepo.findByProductIdAndUserId(1, "user1")).thenReturn(existing);
        when(cartRepo.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.saveCart(1, "user1");

        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(result.getTotalPrice()).isEqualTo(27.0);
    }

    @Test
    @DisplayName("saveCart — product has no discountPrice → uses regular price")
    void saveCart_NoDiscountPrice_UsesRegularPrice() {
        product.setDiscountPrice(null);
        when(productRepo.findById(1)).thenReturn(Optional.of(product));
        when(cartRepo.findByProductIdAndUserId(1, "user1")).thenReturn(null);
        when(cartRepo.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.saveCart(1, "user1");

        assertThat(result.getTotalPrice()).isEqualTo(10.0);
    }

    // ── getCartsByUser ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getCartsByUser — returns carts with recalculated totals")
    void getCartsByUser_ReturnsCarts() {
        Cart cart = new Cart();
        cart.setProduct(product);
        cart.setQuantity(2);
        when(cartRepo.findByUserId("user1")).thenReturn(List.of(cart));

        List<Cart> result = cartService.getCartsByUser("user1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalPrice()).isEqualTo(18.0); // 9.0 * 2
        assertThat(result.get(0).getTotalOrderPrice()).isEqualTo(18.0);
    }

    // ── getCountCart ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCountCart — delegates to repo.countByUserId")
    void getCountCart_DelegatesCorrectly() {
        when(cartRepo.countByUserId("user1")).thenReturn(5);
        assertThat(cartService.getCountCart("user1")).isEqualTo(5);
    }

    // ── updateQuantity ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateQuantity 'in' — increments cart quantity")
    void updateQuantity_Increment() {
        Cart cart = new Cart();
        cart.setProduct(product);
        cart.setQuantity(2);
        when(cartRepo.findById(1)).thenReturn(Optional.of(cart));

        cartService.updateQuantity("in", 1);

        verify(cartRepo).save(cart);
        assertThat(cart.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("updateQuantity 'de' — decrements cart quantity")
    void updateQuantity_Decrement() {
        Cart cart = new Cart();
        cart.setProduct(product);
        cart.setQuantity(3);
        when(cartRepo.findById(1)).thenReturn(Optional.of(cart));

        cartService.updateQuantity("de", 1);

        verify(cartRepo).save(cart);
        assertThat(cart.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateQuantity 'de' to 0 — deletes cart entry")
    void updateQuantity_DecrementToZero_DeletesCart() {
        Cart cart = new Cart();
        cart.setProduct(product);
        cart.setQuantity(1);
        when(cartRepo.findById(1)).thenReturn(Optional.of(cart));

        cartService.updateQuantity("de", 1);

        verify(cartRepo).delete(cart);
        verify(cartRepo, never()).save(any());
    }
}
