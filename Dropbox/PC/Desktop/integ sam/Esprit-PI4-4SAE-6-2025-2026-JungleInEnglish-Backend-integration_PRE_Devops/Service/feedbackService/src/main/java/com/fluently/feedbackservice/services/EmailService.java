package com.fluently.feedbackservice.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendRepetitivePatternAlert(String professorEmail, Long lessonId,
                                           Map<String, List<String>> patternToComments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(professorEmail);
            helper.setSubject("⚠️ Repetitive issues detected in your lesson #" + lessonId);

            StringBuilder html = new StringBuilder();
            html.append("<div style='font-family: Arial, sans-serif; max-width: 600px;'>");
            html.append("<h2 style='color: #e74c3c;'>Repetitive Pattern Detected</h2>");
            html.append("<p>Dear Professor,</p>");
            html.append("<p>We noticed that students are repeatedly reporting similar issues ");
            html.append("in <strong>Lesson #").append(lessonId).append("</strong>. ");
            html.append("Please check and fix the problem as soon as possible.</p>");

            html.append("<hr style='border: 1px solid #eee;'/>");

            for (Map.Entry<String, List<String>> entry : patternToComments.entrySet()) {
                String keyword = entry.getKey();
                List<String> comments = entry.getValue();

                html.append("<div style='margin: 16px 0; padding: 12px; ");
                html.append("background: #fdf3f3; border-left: 4px solid #e74c3c; border-radius: 4px;'>");
                html.append("<p style='margin: 0 0 8px; font-weight: bold; color: #c0392b;'>");
                html.append("Keyword flagged: \"").append(keyword).append("\" — mentioned in ");
                html.append(comments.size()).append(" feedback(s)</p>");
                html.append("<ul style='margin: 0; padding-left: 20px;'>");

                for (String comment : comments) {
                    html.append("<li style='margin: 4px 0; color: #555;'>\"")
                            .append(escapeHtml(comment))
                            .append("\"</li>");
                }

                html.append("</ul></div>");
            }

            html.append("<hr style='border: 1px solid #eee;'/>");
            html.append("<p style='color: #888; font-size: 12px;'>This is an automated alert ");
            html.append("from Jungle in English. Please do not reply to this email.</p>");
            html.append("</div>");

            helper.setText(html.toString(), true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to professor: " + e.getMessage());
        }
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}