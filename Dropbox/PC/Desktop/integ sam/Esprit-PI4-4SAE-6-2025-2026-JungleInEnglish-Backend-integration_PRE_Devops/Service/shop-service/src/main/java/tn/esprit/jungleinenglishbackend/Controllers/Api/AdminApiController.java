package tn.esprit.jungleinenglishbackend.Controllers.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.jungleinenglishbackend.Reposotories.CategoryRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductOrderRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private ProductOrderRepo orderRepo;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return Map.of(
                "totalProducts", productRepo.count(),
                "totalCategories", categoryRepo.count(),
                "totalOrders", orderRepo.count());
    }
}
