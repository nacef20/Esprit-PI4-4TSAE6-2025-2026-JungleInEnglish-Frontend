package tn.esprit.jungleinenglishbackend.Services;

import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import tn.esprit.jungleinenglishbackend.Models.dto.PaymentInitiateRequest;
import tn.esprit.jungleinenglishbackend.Models.dto.PaymentConfirmRequest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {
    
    private final JavaMailSender mailSender;
    
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String senderEmail;
    
    public PaymentService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // In-memory store for mock sessions
    private final Map<String, Map<String, String>> sessionStore = new ConcurrentHashMap<>();

    public Map<String, Object> initiatePayment(PaymentInitiateRequest request) {
        String sessionId = UUID.randomUUID().toString();
        // store the session as internal status "PENDING" and save email
        sessionStore.put(sessionId, new ConcurrentHashMap<>(Map.of(
            "status", "PENDING",
            "email", request.getEmail() != null ? request.getEmail() : "",
            "orderDetails", request.getOrderDetailsText() != null ? request.getOrderDetailsText() : ""
        )));
        
        return Map.of(
                "sessionId", sessionId,
                "status", "PENDING",
                "message", "Payment session initiated successfully."
        );
    }

    public Map<String, Object> confirmPayment(PaymentConfirmRequest request) {
        if (!sessionStore.containsKey(request.getSessionId())) {
            return Map.of(
                    "sessionId", request.getSessionId() != null ? request.getSessionId() : "",
                    "status", "FAILED",
                    "message", "Invalid or expired session ID."
            );
        }
        
        // Mock verification: Always accept any non-null card for testing
        if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            return Map.of(
                    "sessionId", request.getSessionId(),
                    "status", "FAILED",
                    "message", "Payment declined. Card number cannot be blank."
            );
        }
        
        // Mark session as paid
        Map<String, String> sessionInfo = sessionStore.get(request.getSessionId());
        sessionInfo.put("status", "PAID");
        
        // Send Email Confirmation
        String userEmail = sessionInfo.get("email");
        String orderDetails = sessionInfo.get("orderDetails");
        if (userEmail != null && !userEmail.isEmpty()) {
            sendPaymentConfirmationMail(userEmail, request.getSessionId(), orderDetails);
        }
        
        return Map.of(
                "sessionId", request.getSessionId(),
                "status", "SUCCESS",
                "message", "Payment confirmed successfully!"
        );
    }
    
    private void sendPaymentConfirmationMail(String toEmail, String sessionId, String orderDetails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Jungle Store <" + senderEmail + ">");
            message.setTo(toEmail);
            message.setSubject("Jungle Payment Receipt");
            
            String text = "Hello,\n\nYour payment for session ID " + sessionId + " was successful!\n\n" + orderDetails;
            message.setText(text);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send payment confirmation email: " + e.getMessage());
        }
    }
}
