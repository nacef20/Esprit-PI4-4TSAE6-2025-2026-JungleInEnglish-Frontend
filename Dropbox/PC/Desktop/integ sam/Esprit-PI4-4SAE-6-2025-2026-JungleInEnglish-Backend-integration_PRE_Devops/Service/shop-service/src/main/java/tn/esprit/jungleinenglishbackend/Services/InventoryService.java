package tn.esprit.jungleinenglishbackend.Services;

import tn.esprit.jungleinenglishbackend.Models.dto.InventoryAlertDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.InventoryReportDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.ProductSalesDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.RestockResponseDTO;

import java.util.List;

/**
 * Service interface for inventory analysis and management operations.
 * Provides stock monitoring, sales analysis, smart restocking, and reporting.
 */
public interface InventoryService {

    /**
     * Retrieves all products whose stock is below their minimum threshold.
     *
     * @return list of low-stock alerts, empty if all products are sufficiently
     *         stocked
     */
    List<InventoryAlertDTO> getLowStockAlerts();

    /**
     * Retrieves the top-selling products ranked by total quantity sold.
     *
     * @param limit maximum number of products to return (must be > 0)
     * @return ranked list of top sellers with sales metrics
     */
    List<ProductSalesDTO> getTopSellingProducts(int limit);

    /**
     * Generates a comprehensive inventory report classifying products as
     * fast-moving, slow-moving, or out-of-stock.
     *
     * @return the full inventory report
     */
    InventoryReportDTO generateInventoryReport();

    /**
     * Calculates the recommended restock quantity for a product based on
     * historical sales data and the product's minimum stock threshold.
     *
     * @param productId the product ID to analyze
     * @return recommended number of units to order
     */
    int calculateRecommendedRestock(Integer productId);

    /**
     * Restocks a product by adding inventory units.
     * If quantity is null, automatically uses the recommended restock amount.
     *
     * @param productId the product to restock
     * @param quantity  explicit quantity to add (nullable — uses recommendation if
     *                  null)
     * @return details of the restocking operation
     */
    RestockResponseDTO restockProduct(Integer productId, Integer quantity);
}
