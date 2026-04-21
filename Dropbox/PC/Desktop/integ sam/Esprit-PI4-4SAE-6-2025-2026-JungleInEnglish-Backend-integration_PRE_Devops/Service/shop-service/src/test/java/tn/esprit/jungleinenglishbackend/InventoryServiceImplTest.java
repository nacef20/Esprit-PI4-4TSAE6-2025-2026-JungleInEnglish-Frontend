package tn.esprit.jungleinenglishbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Models.dto.InventoryAlertDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.InventoryReportDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.ProductSalesDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.RestockResponseDTO;
import tn.esprit.jungleinenglishbackend.Reposotories.InventoryRepository;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.impl.InventoryServiceImpl;
import tn.esprit.jungleinenglishbackend.exceptions.ProductNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryServiceImpl.
 * Uses Mockito to isolate the service layer from database dependencies.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1);
        sampleProduct.setTitle("Jungle English Book");
        sampleProduct.setStock(3);
        sampleProduct.setMinStockLevel(10);
        sampleProduct.setPrice(29.99);
        sampleProduct.setIsActive(true);
    }

    // =========================================================================
    // Low-Stock Alert Tests
    // =========================================================================

    @Test
    @DisplayName("getLowStockAlerts — should return alerts for products below threshold")
    void testGetLowStockAlerts_ReturnsCorrectAlerts() {
        // Arrange: product has stock=3, minStockLevel=10 → deficit=7
        when(inventoryRepository.findProductsBelowMinStock())
                .thenReturn(List.of(sampleProduct));

        // Act
        List<InventoryAlertDTO> alerts = inventoryService.getLowStockAlerts();

        // Assert
        assertNotNull(alerts);
        assertEquals(1, alerts.size());

        InventoryAlertDTO alert = alerts.get(0);
        assertEquals(1, alert.getProductId());
        assertEquals("Jungle English Book", alert.getProductTitle());
        assertEquals(3, alert.getCurrentStock());
        assertEquals(10, alert.getMinStockLevel());
        assertEquals(7, alert.getDeficit());  // 10 - 3 = 7

        verify(inventoryRepository, times(1)).findProductsBelowMinStock();
    }

    // =========================================================================
    // Top-Selling Products Tests
    // =========================================================================

    @Test
    @DisplayName("getTopSellingProducts — should return top N products by quantity sold")
    void testGetTopSellingProducts_ReturnsTopN() {
        // Arrange
        Object[] row = new Object[] { sampleProduct, 150L, 4498.50 };
        List<Object[]> resultList = Collections.singletonList(row);
        when(inventoryRepository.findTopSellingProducts(PageRequest.of(0, 5)))
                .thenReturn(resultList);

        // Act
        List<ProductSalesDTO> topProducts = inventoryService.getTopSellingProducts(5);

        // Assert
        assertNotNull(topProducts);
        assertEquals(1, topProducts.size());

        ProductSalesDTO dto = topProducts.get(0);
        assertEquals(1, dto.getProductId());
        assertEquals(150L, dto.getTotalQuantitySold());
        assertEquals(4498.50, dto.getTotalRevenue());

        verify(inventoryRepository).findTopSellingProducts(PageRequest.of(0, 5));
    }

    // =========================================================================
    // Inventory Report Tests
    // =========================================================================

    @Test
    @DisplayName("generateInventoryReport — should classify products by fast/slow/out-of-stock")
    void testGenerateInventoryReport_ClassifiesCorrectly() {
        // Arrange: 2 products — one fast (100 sold), one slow (5 sold)
        Product fastProduct = new Product();
        fastProduct.setId(1);
        fastProduct.setTitle("Fast Seller");
        fastProduct.setStock(50);
        fastProduct.setMinStockLevel(10);

        Product slowProduct = new Product();
        slowProduct.setId(2);
        slowProduct.setTitle("Slow Seller");
        slowProduct.setStock(100);
        slowProduct.setMinStockLevel(10);

        Product outOfStockProduct = new Product();
        outOfStockProduct.setId(3);
        outOfStockProduct.setTitle("Out of Stock Item");
        outOfStockProduct.setStock(0);
        outOfStockProduct.setMinStockLevel(10);

        Object[] fastRow = new Object[] { fastProduct, 100L, 2999.00 };
        Object[] slowRow = new Object[] { slowProduct, 5L, 149.95 };
        List<Object[]> salesList = Arrays.asList(fastRow, slowRow);

        when(inventoryRepository.findAllProductSales())
                .thenReturn(salesList);
        when(inventoryRepository.findOutOfStockProducts())
                .thenReturn(List.of(outOfStockProduct));

        // Act
        InventoryReportDTO report = inventoryService.generateInventoryReport();

        // Assert
        assertNotNull(report);
        assertNotNull(report.getGeneratedAt());

        // Median of [100, 5] = (100+5)/2 = 52 → fast > 52, slow ≤ 52
        assertEquals(1, report.getFastMovingProducts().size());
        assertEquals("Fast Seller", report.getFastMovingProducts().get(0).getProductTitle());

        assertEquals(1, report.getSlowMovingProducts().size());
        assertEquals("Slow Seller", report.getSlowMovingProducts().get(0).getProductTitle());

        assertEquals(1, report.getOutOfStockProducts().size());
        assertEquals("Out of Stock Item", report.getOutOfStockProducts().get(0).getProductTitle());
    }

    // =========================================================================
    // Smart Restocking Tests
    // =========================================================================

    @Test
    @DisplayName("calculateRecommendedRestock — with sales history, returns computed quantity")
    void testCalculateRecommendedRestock_WithSalesHistory() {
        // Arrange: product has stock=3, minStockLevel=10, avgPerOrder=5.0
        // salesBasedTarget = ceil(5.0 × 3) = 15
        // target = max(10, 15) = 15
        // recommended = 15 - 3 = 12
        when(productRepo.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(inventoryRepository.findAverageQuantitySoldPerOrder(1)).thenReturn(5.0);

        // Act
        int recommended = inventoryService.calculateRecommendedRestock(1);

        // Assert
        assertEquals(12, recommended);
    }

    @Test
    @DisplayName("calculateRecommendedRestock — with no sales history, defaults to minStockLevel")
    void testCalculateRecommendedRestock_WithNoSalesHistory() {
        // Arrange: product has stock=3, minStockLevel=10, avgPerOrder=0.0
        // salesBasedTarget = ceil(0.0 × 3) = 0
        // target = max(10, 0) = 10
        // recommended = 10 - 3 = 7
        when(productRepo.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(inventoryRepository.findAverageQuantitySoldPerOrder(1)).thenReturn(0.0);

        // Act
        int recommended = inventoryService.calculateRecommendedRestock(1);

        // Assert
        assertEquals(7, recommended);
    }

    // =========================================================================
    // Restock Product Tests
    // =========================================================================

    @Test
    @DisplayName("restockProduct — with explicit quantity, adds specified amount")
    void testRestockProduct_WithExplicitQuantity() {
        // Arrange
        when(productRepo.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(inventoryRepository.findAverageQuantitySoldPerOrder(1)).thenReturn(5.0);
        when(productRepo.save(any(Product.class))).thenReturn(sampleProduct);

        // Act
        RestockResponseDTO response = inventoryService.restockProduct(1, 20);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getProductId());
        assertEquals(3, response.getPreviousStock());
        assertEquals(20, response.getAddedQuantity());
        assertEquals(23, response.getNewStock());  // 3 + 20
        assertNotNull(response.getRestockedAt());

        verify(productRepo).save(sampleProduct);
    }

    @Test
    @DisplayName("restockProduct — with null quantity, uses recommended amount")
    void testRestockProduct_WithRecommendedQuantity() {
        // Arrange: recommended = max(10, ceil(5×3)) - 3 = 15 - 3 = 12
        when(productRepo.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(inventoryRepository.findAverageQuantitySoldPerOrder(1)).thenReturn(5.0);
        when(productRepo.save(any(Product.class))).thenReturn(sampleProduct);

        // Act
        RestockResponseDTO response = inventoryService.restockProduct(1, null);

        // Assert
        assertNotNull(response);
        assertEquals(12, response.getAddedQuantity());
        assertEquals(15, response.getNewStock());  // 3 + 12

        verify(productRepo).save(sampleProduct);
    }

    @Test
    @DisplayName("restockProduct — with invalid product ID, throws ProductNotFoundException")
    void testRestockProduct_InvalidProduct_ThrowsException() {
        // Arrange
        when(productRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
                inventoryService.restockProduct(999, 10));

        verify(productRepo, never()).save(any());
    }
}
