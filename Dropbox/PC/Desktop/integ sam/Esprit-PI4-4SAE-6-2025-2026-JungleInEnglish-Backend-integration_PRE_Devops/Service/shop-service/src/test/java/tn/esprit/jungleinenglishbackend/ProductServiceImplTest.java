package tn.esprit.jungleinenglishbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Reposotories.CartRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductOrderRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.impl.ProductServiceImpl;
import tn.esprit.jungleinenglishbackend.exceptions.InvalidInputException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepo productRepository;
    @Mock private CartRepo cartRepository;
    @Mock private ProductOrderRepo productOrderRepository;
    @InjectMocks private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1);
        product.setTitle("Jungle English Book");
        product.setCategory("Grammar");
        product.setPrice(20.0);
        product.setDiscount(10);
        product.setStock(5);
        product.setIsActive(true);
    }

    // ── saveProduct ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveProduct — new product without image → saves with discount price calculated")
    void saveProduct_New_CalculatesDiscountPrice() {
        when(productRepository.existsByTitleIgnoreCaseAndCategoryIgnoreCase("Jungle English Book", "Grammar"))
                .thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.saveProduct(product, null);

        // discountPrice = 20 - (20 * 10/100) = 18.0
        assertThat(result.getDiscountPrice()).isEqualTo(18.0);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("saveProduct — duplicate title+category → throws InvalidInputException")
    void saveProduct_Duplicate_ThrowsException() {
        when(productRepository.existsByTitleIgnoreCaseAndCategoryIgnoreCase("Jungle English Book", "Grammar"))
                .thenReturn(true);

        assertThatThrownBy(() -> productService.saveProduct(product, null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
    }

    // ── getAllProducts ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllProducts — returns full list from repository")
    void getAllProducts_ReturnsAll() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        assertThat(productService.getAllProducts()).hasSize(1);
    }

    // ── getProductById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getProductById — existing ID → returns product")
    void getProductById_Found() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        assertThat(productService.getProductById(1)).isNotNull();
    }

    @Test
    @DisplayName("getProductById — non-existing ID → returns null")
    void getProductById_NotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThat(productService.getProductById(99)).isNull();
    }

    // ── deleteProduct ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct — existing product → removes from repo and returns true")
    void deleteProduct_Existing_ReturnsTrue() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        Boolean result = productService.deleteProduct(1);

        assertThat(result).isTrue();
        verify(productOrderRepository).deleteByProductId(1);
        verify(cartRepository).deleteByProductId(1);
        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("deleteProduct — non-existing product → returns false")
    void deleteProduct_NonExisting_ReturnsFalse() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThat(productService.deleteProduct(99)).isFalse();
        verify(productRepository, never()).delete(any());
    }

    // ── getAllActiveProducts ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAllActiveProducts — returns only active products")
    void getAllActiveProducts_ReturnsActive() {
        when(productRepository.findByIsActiveTrueOrIsActiveIsNull()).thenReturn(List.of(product));
        List<Product> result = productService.getAllActiveProducts(null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
    }

    // ── updateProduct ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProduct — no duplicate → updates fields and recalculates discount")
    void updateProduct_Valid_UpdatesAndReturns() {
        product.setDiscount(20);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.findByTitleIgnoreCaseAndCategoryIgnoreCase("Jungle English Book", "Grammar"))
                .thenReturn(Optional.of(product)); // same product (same id) → no conflict
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(product, null);

        assertThat(result).isNotNull();
        // discount 20% of 20 = 4 → discountPrice = 16.0
        assertThat(result.getDiscountPrice()).isEqualTo(16.0);
    }
}
