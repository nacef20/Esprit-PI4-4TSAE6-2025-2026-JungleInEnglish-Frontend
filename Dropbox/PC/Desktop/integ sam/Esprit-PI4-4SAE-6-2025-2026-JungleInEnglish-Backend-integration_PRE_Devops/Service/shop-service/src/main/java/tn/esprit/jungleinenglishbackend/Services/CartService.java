package tn.esprit.jungleinenglishbackend.Services;

import tn.esprit.jungleinenglishbackend.Models.Cart;

import java.util.List;

public interface CartService {
    Cart saveCart(Integer productId, String userId);

    List<Cart> getCartsByUser(String userId);

    Integer getCountCart(String userId);

    void updateQuantity(String sy, Integer cid);
}
