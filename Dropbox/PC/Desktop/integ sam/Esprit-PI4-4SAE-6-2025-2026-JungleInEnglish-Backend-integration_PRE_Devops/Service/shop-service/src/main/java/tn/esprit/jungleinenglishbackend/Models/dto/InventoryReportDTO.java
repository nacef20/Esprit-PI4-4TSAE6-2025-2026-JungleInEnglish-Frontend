package tn.esprit.jungleinenglishbackend.Models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing a comprehensive inventory report.
 * Categories products into fast-moving, slow-moving, and out-of-stock buckets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportDTO {

    /** Products with sales volume above the median — high demand items. */
    private List<ProductSalesDTO> fastMovingProducts;

    /** Products with sales volume at or below the median — low demand items. */
    private List<ProductSalesDTO> slowMovingProducts;

    /** Products currently with zero stock. */
    private List<InventoryAlertDTO> outOfStockProducts;

    /** Timestamp when this report was generated. */
    private LocalDateTime generatedAt;
}
