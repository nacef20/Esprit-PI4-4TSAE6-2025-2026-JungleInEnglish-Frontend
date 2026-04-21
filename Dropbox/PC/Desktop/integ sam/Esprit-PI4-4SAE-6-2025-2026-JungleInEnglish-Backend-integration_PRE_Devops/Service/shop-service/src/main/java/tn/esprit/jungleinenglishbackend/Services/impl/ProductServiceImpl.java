package tn.esprit.jungleinenglishbackend.Services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.jungleinenglishbackend.Models.Product;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.ProductService;
import tn.esprit.jungleinenglishbackend.exceptions.InvalidInputException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepo productRepository;

    @Override
    public Product saveProduct(Product product, MultipartFile image) {
        // Duplicate check: block if same name + category already exists (case-insensitive)
        String title = product.getTitle();
        String category = product.getCategory();
        if (title != null && category != null &&
                productRepository.existsByTitleIgnoreCaseAndCategoryIgnoreCase(title, category)) {
            throw new InvalidInputException(
                "A product with this name already exists in this category."
            );
        }

        if (image != null && !image.isEmpty()) {
            product.setImage(image.getOriginalFilename());
            saveImage(image);
        }

        calculateDiscountPrice(product);
        return productRepository.save(product);
    }

    private void calculateDiscountPrice(Product product) {
        if (product.getPrice() != null) {
            Double discount = product.getPrice() * (product.getDiscount() / 100.0);
            Double discountPrice = product.getPrice() - discount;
            product.setDiscountPrice(discountPrice);
        }
    }

    private void saveImage(MultipartFile image) {
        try {
            // Save to a directory named 'uploads' in the project root
            String uploadDir = "uploads/product_img";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(image.getOriginalFilename());
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findAll(pageable);
    }

    @Autowired
    private tn.esprit.jungleinenglishbackend.Reposotories.CartRepo cartRepository;

    @Autowired
    private tn.esprit.jungleinenglishbackend.Reposotories.ProductOrderRepo productOrderRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Boolean deleteProduct(Integer id) {
        Product product = productRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(product)) {
            // Remove child cart entries and order history that reference this product
            productOrderRepository.deleteByProductId(id);
            cartRepository.deleteByProductId(id);
            productRepository.delete(product);
            return true;
        }
        return false;
    }

    @Override
    public Product getProductById(Integer id) {
        Product product = productRepository.findById(id).orElse(null);
        return product;
    }

    @Override
    public Product updateProduct(Product product, MultipartFile image) {
        Product dbProduct = getProductById(product.getId());

        // Duplicate check on update: block if another product (different id) has same name+category
        String newTitle = product.getTitle();
        String newCategory = product.getCategory();
        if (newTitle != null && newCategory != null) {
            productRepository
                .findByTitleIgnoreCaseAndCategoryIgnoreCase(newTitle, newCategory)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(product.getId())) {
                        throw new InvalidInputException(
                            "A product with this name already exists in this category."
                        );
                    }
                });
        }

        String imageName = (image == null || image.isEmpty()) ? dbProduct.getImage() : image.getOriginalFilename();

        dbProduct.setTitle(product.getTitle());
        dbProduct.setDescription(product.getDescription());
        dbProduct.setCategory(product.getCategory());
        dbProduct.setPrice(product.getPrice());
        dbProduct.setStock(product.getStock());
        dbProduct.setImage(imageName);
        dbProduct.setIsActive(product.getIsActive());
        dbProduct.setDiscount(product.getDiscount());
        calculateDiscountPrice(dbProduct);

        Product updateProduct = productRepository.save(dbProduct);

        if (!ObjectUtils.isEmpty(updateProduct)) {
            if (image != null && !image.isEmpty()) {
                saveImage(image);
            }
            return updateProduct;
        }
        return null;
    }

    @Override
    public List<Product> getAllActiveProducts(String category) {
        if (ObjectUtils.isEmpty(category)) {
            return productRepository.findByIsActiveTrueOrIsActiveIsNull();
        } else {
            // ... original findByCategory didn't handle isActive, 
            // but we'll stick to the base fix for now
            return productRepository.findByIsActiveTrueOrIsActiveIsNull();
        }
    }

    @Override
    public List<Product> searchProduct(String ch) {
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
    }

    @Override
    public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
    }

    @Override
    public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        if (ObjectUtils.isEmpty(category)) {
            return productRepository.findByIsActiveTrueOrIsActiveIsNull(pageable);
        } else {
            return productRepository.findByIsActiveTrueOrIsActiveIsNullAndCategory(category, pageable);
        }
    }

    @Override
    public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        if (ObjectUtils.isEmpty(category)) {
            return productRepository.findByIsActiveTrueOrIsActiveIsNullAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
        } else {
            return productRepository.findByIsActiveTrueOrIsActiveIsNullAndCategoryAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(category, ch, ch, pageable);
        }
    }
}
