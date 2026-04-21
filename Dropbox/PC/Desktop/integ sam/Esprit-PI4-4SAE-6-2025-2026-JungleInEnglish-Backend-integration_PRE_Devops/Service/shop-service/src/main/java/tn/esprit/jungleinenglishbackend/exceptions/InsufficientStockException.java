package tn.esprit.jungleinenglishbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an inventory operation cannot be completed due to insufficient
 * stock levels.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(Integer productId, int requested, int available) {
        super(String.format(
                "Insufficient stock for product ID %d: requested=%d, available=%d",
                productId, requested, available));
    }
}
