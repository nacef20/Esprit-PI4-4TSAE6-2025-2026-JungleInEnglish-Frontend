package tn.esprit.jungleinenglishbackend.Controllers.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.jungleinenglishbackend.Models.TotalMoneyDTO;
import tn.esprit.jungleinenglishbackend.Services.OrderService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderApiController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/total-money")
    public ResponseEntity<TotalMoneyDTO> getTotalMoneyForAllUsers() {
        BigDecimal total = orderService.getTotalMoneyForAllUsers();
        return ResponseEntity.ok(new TotalMoneyDTO(total));
    }
}
