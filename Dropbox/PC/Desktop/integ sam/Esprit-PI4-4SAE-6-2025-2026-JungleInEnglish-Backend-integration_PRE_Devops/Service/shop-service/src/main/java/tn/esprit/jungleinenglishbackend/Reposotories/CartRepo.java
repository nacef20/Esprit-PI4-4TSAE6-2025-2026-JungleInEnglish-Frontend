package tn.esprit.jungleinenglishbackend.Reposotories;

import tn.esprit.jungleinenglishbackend.Models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartRepo extends JpaRepository<Cart, Integer> {

    Cart findByProductIdAndUserId(Integer productId, String userId);

    List<Cart> findByUserId(String userId);

    Integer countByUserId(String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    void deleteByProductId(Integer productId);
}
