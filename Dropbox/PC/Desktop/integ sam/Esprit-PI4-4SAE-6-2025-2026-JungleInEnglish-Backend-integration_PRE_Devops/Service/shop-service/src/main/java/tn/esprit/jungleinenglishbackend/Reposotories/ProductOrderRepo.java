package tn.esprit.jungleinenglishbackend.Reposotories;

import tn.esprit.jungleinenglishbackend.Models.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductOrderRepo extends JpaRepository<ProductOrder, Integer> {

    ProductOrder findByOrderId(String orderId);

    List<ProductOrder> findByUserId(String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductOrder o WHERE o.product.id = :productId")
    void deleteByProductId(@Param("productId") Integer productId);

    @Query("SELECT COALESCE(SUM(o.price * o.quantity), 0) FROM ProductOrder o WHERE o.userId = :userId")
    Double sumTotalAmountByUserId(@Param("userId") String userId);

    @Query("SELECT COALESCE(SUM(o.price * o.quantity), 0) FROM ProductOrder o")
    Double sumTotalAmountAllUsers();
}
