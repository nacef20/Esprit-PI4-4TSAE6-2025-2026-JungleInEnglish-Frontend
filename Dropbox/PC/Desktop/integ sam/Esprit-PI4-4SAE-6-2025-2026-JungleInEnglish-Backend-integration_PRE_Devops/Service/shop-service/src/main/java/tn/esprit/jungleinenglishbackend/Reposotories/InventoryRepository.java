package tn.esprit.jungleinenglishbackend.Reposotories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Models.ProductOrder;

import java.util.List;

/**
 * Repository dedicated to inventory analysis queries.
 * Uses JPQL to aggregate sales data and identify stock-level anomalies.
 *
 * NOTE: Extends JpaRepository<ProductOrder, Integer> because the majority of
 * queries are aggregated from the ProductOrder table. Product-centric queries
 * use direct JPQL on the Product entity.
 */
public interface InventoryRepository extends JpaRepository<ProductOrder, Integer> {

    // =========================================================================
    // Stock Monitoring Queries
    // =========================================================================

    /**
     * Finds all products where current stock is below the minimum threshold.
     * Used for low-stock alert generation.
     */
    @Query("SELECT p FROM Product p WHERE p.stock < p.minStockLevel AND p.stock > 0")
    List<Product> findProductsBelowMinStock();

    /**
     * Finds all products that are completely out of stock.
     */
    @Query("SELECT p FROM Product p WHERE p.stock = 0")
    List<Product> findOutOfStockProducts();

    // =========================================================================
    // Sales Analysis Queries
    // =========================================================================

    /**
     * Retrieves the top-selling products ordered by total quantity sold
     * (descending).
     * Returns Object arrays: [Product, totalQuantitySold (Long), totalRevenue
     * (Double)].
     *
     * @param pageable controls the number of results (limit)
     */
    @Query("SELECT po.product, SUM(po.quantity) AS totalQty, SUM(po.price * po.quantity) AS totalRev " +
            "FROM ProductOrder po " +
            "GROUP BY po.product " +
            "ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);

    /**
     * Retrieves ALL products with their sales aggregations, ordered by quantity
     * sold descending.
     * Used for inventory report generation (classifying fast vs. slow movers).
     */
    @Query("SELECT po.product, SUM(po.quantity) AS totalQty, SUM(po.price * po.quantity) AS totalRev " +
            "FROM ProductOrder po " +
            "GROUP BY po.product " +
            "ORDER BY totalQty DESC")
    List<Object[]> findAllProductSales();

    /**
     * Calculates the total quantity sold for a specific product.
     *
     * @param productId the ID of the product
     * @return total units sold, or null if no orders exist
     */
    @Query("SELECT COALESCE(SUM(po.quantity), 0) FROM ProductOrder po WHERE po.product.id = :productId")
    Long findTotalQuantitySoldByProduct(@Param("productId") Integer productId);

    /**
     * Calculates the average quantity sold per order for a specific product.
     * Used in the smart restocking algorithm to estimate future demand.
     *
     * @param productId the ID of the product
     * @return average quantity per order, or null if no orders exist
     */
    @Query("SELECT COALESCE(AVG(po.quantity), 0) FROM ProductOrder po WHERE po.product.id = :productId")
    Double findAverageQuantitySoldPerOrder(@Param("productId") Integer productId);

    /**
     * Counts the total number of orders for a specific product.
     * Used alongside average to determine demand consistency.
     *
     * @param productId the ID of the product
     * @return total number of orders containing this product
     */
    @Query("SELECT COUNT(po) FROM ProductOrder po WHERE po.product.id = :productId")
    Long countOrdersByProduct(@Param("productId") Integer productId);
}
