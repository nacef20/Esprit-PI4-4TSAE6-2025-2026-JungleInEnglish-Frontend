package tn.esprit.jungleinenglishbackend.Models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a low-stock alert for a specific product.
 * Contains the current stock level and the deficit relative to the minimum
 * threshold.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAlertDTO {

    private Integer productId;
    private String productTitle;
    private int currentStock;
    private int minStockLevel;

    /**
     * How many units below the threshold this product currently sits.
     * Computed as: minStockLevel - currentStock
     */
    private int deficit;
}
