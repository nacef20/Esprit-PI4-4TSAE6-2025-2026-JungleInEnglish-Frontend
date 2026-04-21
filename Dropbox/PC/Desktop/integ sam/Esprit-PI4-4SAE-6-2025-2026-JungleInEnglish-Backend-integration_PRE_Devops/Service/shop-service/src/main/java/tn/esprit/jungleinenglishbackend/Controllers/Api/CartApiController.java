package tn.esprit.jungleinenglishbackend.Controllers.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.jungleinenglishbackend.Models.Cart;
import tn.esprit.jungleinenglishbackend.Reposotories.CartRepo;
import tn.esprit.jungleinenglishbackend.Services.CartService;
import tn.esprit.jungleinenglishbackend.security.SecurityUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepo cartRepo;

    @GetMapping
    public List<Cart> getCartByUser() {
        String userId = SecurityUtils.getCurrentUserId();
        return cartService.getCartsByUser(userId);
    }

    @GetMapping("/count")
    public Map<String, Integer> getCartCount() {
        String userId = SecurityUtils.getCurrentUserId();
        return Map.of("count", cartService.getCountCart(userId));
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request) {
        if (request.productId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad Request", "message", "productId is required"));
        }
        String userId = SecurityUtils.getCurrentUserId();
        Cart saved = cartService.saveCart(request.productId, userId);
        if (saved == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error", "message", "Failed to add to cart"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<?> updateQuantity(@PathVariable("id") Integer id, @RequestParam("sy") String sy) {
        cartService.updateQuantity(sy, id);
        return ResponseEntity.ok(Map.of("message", "Quantity updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable("id") Integer id) {
        if (!cartRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cartRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public static class AddToCartRequest {
        public Integer productId;
    }
}
