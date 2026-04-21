package tn.esprit.jungleinenglishbackend.Models.dto;

import lombok.Data;

@Data
public class PaymentConfirmRequest {
    private String sessionId;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
}
