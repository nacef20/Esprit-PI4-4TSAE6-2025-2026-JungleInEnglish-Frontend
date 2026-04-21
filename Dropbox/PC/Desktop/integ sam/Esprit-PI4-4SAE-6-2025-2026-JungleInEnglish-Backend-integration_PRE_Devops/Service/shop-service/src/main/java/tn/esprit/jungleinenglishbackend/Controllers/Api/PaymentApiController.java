package tn.esprit.jungleinenglishbackend.Controllers.Api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.jungleinenglishbackend.Models.dto.PaymentInitiateRequest;
import tn.esprit.jungleinenglishbackend.Models.dto.PaymentConfirmRequest;
import tn.esprit.jungleinenglishbackend.Services.PaymentService;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiate(@RequestBody PaymentInitiateRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@RequestBody PaymentConfirmRequest request) {
        Map<String, Object> response = paymentService.confirmPayment(request);
        if ("FAILED".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
