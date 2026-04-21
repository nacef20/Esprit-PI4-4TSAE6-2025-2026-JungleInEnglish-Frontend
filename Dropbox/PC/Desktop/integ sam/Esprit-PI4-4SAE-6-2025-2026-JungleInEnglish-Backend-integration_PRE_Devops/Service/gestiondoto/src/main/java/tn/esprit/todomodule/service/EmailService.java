package tn.esprit.todomodule.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        System.out.println("EmailService initialized with JavaMailSender.");
    }

    public void sendReminder(String to, String subject, String text) {
        if (to == null || to.isBlank()) {
            System.err.println("Cannot send email: 'to' address is null or blank.");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        try {
            mailSender.send(message);
            System.out.println("SUCCESS: Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to send email to " + to + ". Reason: " + e.getMessage());
            System.err.println("Tip: Check if 'rgpqqabzpzjihexy' is still a valid App Password and that SMTP is enabled.");
        }
    }
}
