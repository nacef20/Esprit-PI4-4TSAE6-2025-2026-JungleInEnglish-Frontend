package tn.esprit.jungleinenglishbackend.Services.impl;

import tn.esprit.jungleinenglishbackend.Models.Cart;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Reposotories.CartRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    @Override
    public Cart saveCart(Integer productId, String userId) {
        Product product = productRepo.findById(productId).get();

        Cart cartStatus = cartRepo.findByProductIdAndUserId(productId, userId);

        Double effectivePrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
        if (effectivePrice == null) effectivePrice = 0.0;

        Cart cart;

        if (ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setProduct(product);
            cart.setUserId(userId);
            cart.setQuantity(1);
            cart.setTotalPrice(1 * effectivePrice);
        } else {
            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * effectivePrice);
        }

        return cartRepo.save(cart);
    }

    @Override
    public List<Cart> getCartsByUser(String userId) {
        List<Cart> carts = cartRepo.findByUserId(userId);

        Double totalOrderPrice = 0.0;
        List<Cart> updateCarts = new ArrayList<>();
        for (Cart c : carts) {
            Double effectivePrice = c.getProduct().getDiscountPrice() != null ? c.getProduct().getDiscountPrice() : c.getProduct().getPrice();
            if (effectivePrice == null) effectivePrice = 0.0;

            Double totalPrice = (effectivePrice * c.getQuantity());
            c.setTotalPrice(totalPrice);
            totalOrderPrice = totalOrderPrice + totalPrice;
            c.setTotalOrderPrice(totalOrderPrice);
            updateCarts.add(c);
        }

        return updateCarts;
    }

    @Override
    public Integer getCountCart(String userId) {
        return cartRepo.countByUserId(userId);
    }

    @Override
    public void updateQuantity(String sy, Integer cid) {
        Cart cart = cartRepo.findById(cid).get();
        int updateQuantity;

        if (sy.equalsIgnoreCase("de")) {
            updateQuantity = cart.getQuantity() - 1;

            if (updateQuantity <= 0) {
                cartRepo.delete(cart);
            } else {
                cart.setQuantity(updateQuantity);
                cartRepo.save(cart);
            }
        } else {
            updateQuantity = cart.getQuantity() + 1;
            cart.setQuantity(updateQuantity);
            cartRepo.save(cart);
        }
    }
}