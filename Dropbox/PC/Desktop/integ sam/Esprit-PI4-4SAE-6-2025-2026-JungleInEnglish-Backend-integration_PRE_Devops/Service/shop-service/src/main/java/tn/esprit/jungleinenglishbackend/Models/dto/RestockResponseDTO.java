package tn.esprit.jungleinenglishbackend.Models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO returned after a successful restocking operation.
 * Shows before/after stock levels and the recommended restock quantity for
 * context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestockResponseDTO {

    private Integer productId;
    private String productTitle;

    /** Stock level before the restock operation. */
    private int previousStock;

    /** Number of units added during this restock. */
    private int addedQuantity;

    /** Stock level after the restock operation. */
    private int newStock;

    /**
     * What the system recommended for restocking (regardless of what was actually
     * added).
     */
    private int recommendedRestockQuantity;

    /** Timestamp of the restocking operation. */
    private LocalDateTime restockedAt;
}
