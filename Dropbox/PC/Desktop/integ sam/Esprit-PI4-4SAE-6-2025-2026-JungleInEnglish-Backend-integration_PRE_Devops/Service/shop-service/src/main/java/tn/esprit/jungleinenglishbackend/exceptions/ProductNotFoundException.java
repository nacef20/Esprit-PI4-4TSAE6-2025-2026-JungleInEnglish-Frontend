package tn.esprit.jungleinenglishbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a product is not found in the database by its ID.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Integer productId) {
        super("Product not found with ID: " + productId);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
