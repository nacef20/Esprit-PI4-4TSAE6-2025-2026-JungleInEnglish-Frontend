package tn.esprit.jungleinenglishbackend.Models.dto;

import lombok.Data;

@Data
public class PaymentInitiateRequest {
    private String email;
    private Double amount;
    private String orderDetailsText;
}
