package tn.esprit.jungleinenglishbackend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.jungleinenglishbackend.exceptions.InsufficientStockException;
import tn.esprit.jungleinenglishbackend.exceptions.InvalidInputException;
import tn.esprit.jungleinenglishbackend.exceptions.ProductNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

                List<Map<String, String>> fieldErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(err -> Map.of(
                                                "field", err.getField(),
                                                "message",
                                                err.getDefaultMessage() != null ? err.getDefaultMessage()
                                                                : "Invalid value"))
                                .collect(Collectors.toList());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("fieldErrors", fieldErrors));
        }

        @ExceptionHandler(ProductNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                                "error", "Product Not Found",
                                                "message", ex.getMessage(),
                                                "timestamp", LocalDateTime.now().toString()));
        }

        @ExceptionHandler(InsufficientStockException.class)
        public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                                "error", "Insufficient Stock",
                                                "message", ex.getMessage(),
                                                "timestamp", LocalDateTime.now().toString()));
        }

        @ExceptionHandler(InvalidInputException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidInput(InvalidInputException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(
                                                "error", "Invalid Input",
                                                "message", ex.getMessage(),
                                                "timestamp", LocalDateTime.now().toString()));
        }
}
