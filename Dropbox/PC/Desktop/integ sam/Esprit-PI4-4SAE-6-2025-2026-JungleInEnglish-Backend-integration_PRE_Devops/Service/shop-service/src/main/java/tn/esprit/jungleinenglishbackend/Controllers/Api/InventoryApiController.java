package tn.esprit.jungleinenglishbackend.Controllers.Api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.jungleinenglishbackend.Models.dto.InventoryAlertDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.InventoryReportDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.ProductSalesDTO;
import tn.esprit.jungleinenglishbackend.Models.dto.RestockResponseDTO;
import tn.esprit.jungleinenglishbackend.Services.InventoryService;

import java.util.List;

/**
 * REST controller for inventory management and analysis endpoints.
 * All endpoints require ADMIN role (enforced via SecurityConfig).
 *
 * Endpoints:
 * - GET /api/inventory/alerts → Low-stock alerts
 * - GET /api/inventory/top-products → Top-selling products
 * - GET /api/inventory/report → Comprehensive inventory report
 * - POST /api/inventory/restock/{id} → Restock a product
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryApiController {

    private final InventoryService inventoryService;

    // =========================================================================
    // GET /api/inventory/alerts
    // =========================================================================

    /**
     * Returns all products whose stock is below their minimum threshold.
     * An empty list means all products are sufficiently stocked.
     *
     * @return 200 OK with list of InventoryAlertDTO
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<InventoryAlertDTO>> getLowStockAlerts() {
        log.info("GET /api/inventory/alerts — fetching low-stock alerts");
        List<InventoryAlertDTO> alerts = inventoryService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    // =========================================================================
    // GET /api/inventory/top-products
    // =========================================================================

    /**
     * Returns the top-selling products ranked by total quantity sold.
     *
     * @param limit optional query parameter to control how many results to return
     *              (default: 10)
     * @return 200 OK with list of ProductSalesDTO
     */
    @GetMapping("/top-products")
    public ResponseEntity<List<ProductSalesDTO>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/inventory/top-products — limit={}", limit);
        List<ProductSalesDTO> topProducts = inventoryService.getTopSellingProducts(limit);
        return ResponseEntity.ok(topProducts);
    }

    // =========================================================================
    // GET /api/inventory/report
    // =========================================================================

    /**
     * Generates a comprehensive inventory report categorizing products as
     * fast-moving, slow-moving, or out-of-stock.
     *
     * @return 200 OK with InventoryReportDTO
     */
    @GetMapping("/report")
    public ResponseEntity<InventoryReportDTO> getInventoryReport() {
        log.info("GET /api/inventory/report — generating inventory report");
        InventoryReportDTO report = inventoryService.generateInventoryReport();
        return ResponseEntity.ok(report);
    }

    // =========================================================================
    // POST /api/inventory/restock/{productId}
    // =========================================================================

    /**
     * Restocks a product by adding units to its current inventory.
     * If no quantity is provided, the system automatically calculates
     * the recommended restock amount based on historical sales data.
     *
     * @param productId path variable — the product to restock
     * @param quantity  optional query parameter — explicit quantity to add
     * @return 200 OK with RestockResponseDTO containing before/after stock levels
     */
    @PostMapping("/restock/{productId}")
    public ResponseEntity<RestockResponseDTO> restockProduct(
            @PathVariable Integer productId,
            @RequestParam(required = false) Integer quantity) {
        log.info("POST /api/inventory/restock/{} — quantity={}", productId,
                quantity != null ? quantity : "auto");
        RestockResponseDTO response = inventoryService.restockProduct(productId, quantity);
        return ResponseEntity.ok(response);
    }
}
