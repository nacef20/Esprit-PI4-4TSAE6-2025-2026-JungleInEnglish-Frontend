package tn.esprit.jungleinenglishbackend.Controllers.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.ProductService;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepo productRepo;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "search", defaultValue = "") String search) {
        Page<Product> page;
        if (search != null && !search.isBlank()) {
            page = productService.searchProductPagination(pageNo, pageSize, search);
        } else {
            page = productService.getAllProductsPagination(pageNo, pageSize);
        }
        return ResponseEntity.ok(Map.of(
                "content", page.getContent(),
                "pageNo", page.getNumber(),
                "pageSize", page.getSize(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "first", page.isFirst(),
                "last", page.isLast()));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActive(
            @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "12") int pageSize,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "search", defaultValue = "") String search) {
        Page<Product> page;
        if (search != null && !search.isBlank()) {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category, search);
        } else {
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        }
        return ResponseEntity.ok(Map.of(
                "content", page.getContent(),
                "pageNo", page.getNumber(),
                "pageSize", page.getSize(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "first", page.isFirst(),
                "last", page.isLast()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") int id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", "Product not found"));
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<?> create(@RequestPart("product") Product product,
            @RequestPart("imageFile") org.springframework.web.multipart.MultipartFile imageFile) {
        if (product.getTitle() == null || product.getTitle().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad Request", "message", "Product title is required"));
        }
        if (product.getIsActive() == null)
            product.setIsActive(true);

        Product saved = productService.saveProduct(product, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<?> update(@PathVariable("id") int id,
            @RequestPart("product") Product product,
            @RequestPart(value = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile) {
        Product existing = productService.getProductById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", "Product not found"));
        }

        // Pass the updated data and the new file (if any) to the service
        product.setId(id);
        Product saved = productService.updateProduct(product, imageFile);

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") int id) {
        if (!productRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
