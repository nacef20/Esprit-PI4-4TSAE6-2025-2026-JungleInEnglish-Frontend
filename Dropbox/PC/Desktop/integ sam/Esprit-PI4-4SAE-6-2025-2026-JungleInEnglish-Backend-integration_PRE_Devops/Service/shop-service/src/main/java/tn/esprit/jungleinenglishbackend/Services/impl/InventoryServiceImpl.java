package tn.esprit.jungleinenglishbackend.Services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Models.dto.InventoryAlertDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.InventoryReportDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.ProductSalesDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.RestockResponseDTO;
import tn.esprit.jungleinenglishbackend.Reposotories.InventoryRepository;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.InventoryService;
import tn.esprit.jungleinenglishbackend.exceptions.InvalidInputException;
import tn.esprit.jungleinenglishbackend.exceptions.ProductNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production implementation of the Inventory Analyst service.
 *
 * Business rules:
 * - Low-stock alerts include products with stock > 0 but below their
 * minStockLevel.
 * - Fast-moving products are those with total sales above the median.
 * - Smart restock formula: max(minStockLevel, avgSalesPerOrder ×
 * RESTOCK_MULTIPLIER) - currentStock.
 * - If the computed restock recommendation is ≤ 0 (already fully stocked), it
 * defaults to minStockLevel.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    /**
     * Multiplier applied to average sales per order when computing restock
     * recommendations.
     * A value of 3 means we recommend stocking enough for roughly 3× the average
     * order size.
     */
    private static final int RESTOCK_MULTIPLIER = 3;

    private final InventoryRepository inventoryRepository;
    private final ProductRepo productRepo;

    // =========================================================================
    // Low-Stock Alerts
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Queries all products where stock < minStockLevel (but stock > 0),
     * and maps each into an InventoryAlertDTO with the computed deficit.
     */
    @Override
    public List<InventoryAlertDTO> getLowStockAlerts() {
        log.info("Generating low-stock alerts");

        List<Product> lowStockProducts = inventoryRepository.findProductsBelowMinStock();

        return lowStockProducts.stream()
                .map(this::mapToAlertDTO)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Top-Selling Products
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Uses a paginated JPQL aggregation query (SUM of quantity, grouped by product)
     * to return the top N sellers. Validates that the limit is positive.
     */
    @Override
    public List<ProductSalesDTO> getTopSellingProducts(int limit) {
        if (limit <= 0) {
            throw new InvalidInputException("Limit must be a positive integer, but got: " + limit);
        }

        log.info("Fetching top {} selling products", limit);

        List<Object[]> results = inventoryRepository.findTopSellingProducts(PageRequest.of(0, limit));

        return results.stream()
                .map(this::mapToSalesDTO)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Inventory Report
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Classification logic:
     * 1. Fetch all products with sales data, sorted by quantity sold (desc).
     * 2. Compute the median sales quantity.
     * 3. Products with total sales > median → fast-moving.
     * 4. Products with total sales ≤ median → slow-moving.
     * 5. Products with stock == 0 are listed in the out-of-stock section.
     */
    @Override
    public InventoryReportDTO generateInventoryReport() {
        log.info("Generating comprehensive inventory report");

        // -- Sales-based classification --
        List<Object[]> allSales = inventoryRepository.findAllProductSales();
        List<ProductSalesDTO> allSalesDTOs = allSales.stream()
                .map(this::mapToSalesDTO)
                .collect(Collectors.toList());

        List<ProductSalesDTO> fastMoving = new ArrayList<>();
        List<ProductSalesDTO> slowMoving = new ArrayList<>();

        if (!allSalesDTOs.isEmpty()) {
            // Calculate median total quantity sold
            long medianQuantity = calculateMedianQuantity(allSalesDTOs);

            for (ProductSalesDTO dto : allSalesDTOs) {
                if (dto.getTotalQuantitySold() > medianQuantity) {
                    fastMoving.add(dto);
                } else {
                    slowMoving.add(dto);
                }
            }
        }

        // -- Out-of-stock products --
        List<Product> outOfStock = inventoryRepository.findOutOfStockProducts();
        List<InventoryAlertDTO> outOfStockAlerts = outOfStock.stream()
                .map(this::mapToAlertDTO)
                .collect(Collectors.toList());

        return InventoryReportDTO.builder()
                .fastMovingProducts(fastMoving)
                .slowMovingProducts(slowMoving)
                .outOfStockProducts(outOfStockAlerts)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // =========================================================================
    // Smart Restocking
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Formula: recommended = max(minStockLevel, ceil(avgSalesPerOrder ×
     * RESTOCK_MULTIPLIER)) - currentStock
     * If the result is ≤ 0 (product is already well-stocked), defaults to
     * minStockLevel.
     */
    @Override
    public int calculateRecommendedRestock(Integer productId) {
        if (productId == null) {
            throw new InvalidInputException("Product ID must not be null");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Double avgPerOrder = inventoryRepository.findAverageQuantitySoldPerOrder(productId);
        int salesBasedTarget = (int) Math.ceil(avgPerOrder * RESTOCK_MULTIPLIER);
        int target = Math.max(product.getMinStockLevel(), salesBasedTarget);
        int recommended = target - product.getStock();

        // If already above target, still recommend at least minStockLevel units
        if (recommended <= 0) {
            recommended = product.getMinStockLevel();
        }

        log.info("Restock recommendation for product {}: {} units (avgPerOrder={}, target={}, currentStock={})",
                productId, recommended, avgPerOrder, target, product.getStock());

        return recommended;
    }

    /**
     * {@inheritDoc}
     *
     * If quantity is null, automatically calculates the recommended amount.
     * Validates that the explicit quantity is positive.
     * Persists the updated stock level.
     */
    @Override
    @Transactional
    public RestockResponseDTO restockProduct(Integer productId, Integer quantity) {
        if (productId == null) {
            throw new InvalidInputException("Product ID must not be null");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        int recommended = calculateRecommendedRestock(productId);
        int addQuantity;

        if (quantity != null) {
            if (quantity <= 0) {
                throw new InvalidInputException("Restock quantity must be positive, but got: " + quantity);
            }
            addQuantity = quantity;
        } else {
            addQuantity = recommended;
        }

        int previousStock = product.getStock();
        product.setStock(previousStock + addQuantity);
        productRepo.save(product);

        log.info("Restocked product {} ('{}'): {} → {} (+{} units)",
                productId, product.getTitle(), previousStock, product.getStock(), addQuantity);

        return RestockResponseDTO.builder()
                .productId(product.getId())
                .productTitle(product.getTitle())
                .previousStock(previousStock)
                .addedQuantity(addQuantity)
                .newStock(product.getStock())
                .recommendedRestockQuantity(recommended)
                .restockedAt(LocalDateTime.now())
                .build();
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    /**
     * Maps a Product entity to an InventoryAlertDTO.
     */
    private InventoryAlertDTO mapToAlertDTO(Product product) {
        return InventoryAlertDTO.builder()
                .productId(product.getId())
                .productTitle(product.getTitle())
                .currentStock(product.getStock())
                .minStockLevel(product.getMinStockLevel())
                .deficit(product.getMinStockLevel() - product.getStock())
                .build();
    }

    /**
     * Maps a raw JPQL result array [Product, Long, Double] to a ProductSalesDTO.
     */
    private ProductSalesDTO mapToSalesDTO(Object[] row) {
        Product product = (Product) row[0];
        Long totalQty = (Long) row[1];
        Double totalRev = (Double) row[2];

        return ProductSalesDTO.builder()
                .productId(product.getId())
                .productTitle(product.getTitle())
                .totalQuantitySold(totalQty)
                .totalRevenue(totalRev)
                .build();
    }

    /**
     * Calculates the median total quantity sold across a list of ProductSalesDTOs.
     * Assumes the list is already sorted by totalQuantitySold in descending order.
     */
    private long calculateMedianQuantity(List<ProductSalesDTO> salesList) {
        int size = salesList.size();
        if (size == 0) {
            return 0L;
        }

        // The list is sorted DESC from the query, we need the middle value
        if (size % 2 == 1) {
            return salesList.get(size / 2).getTotalQuantitySold();
        } else {
            long lower = salesList.get(size / 2 - 1).getTotalQuantitySold();
            long upper = salesList.get(size / 2).getTotalQuantitySold();
            return (lower + upper) / 2;
        }
    }
}
