package com.fluently.feedbackservice.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "flagged_feedbacks")
public class FlaggedFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "original_content", nullable = false, columnDefinition = "TEXT")
    private String originalContent;

    @Column(name = "cleaned_content", columnDefinition = "TEXT")
    private String cleanedContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FlagStatus status;

    @CreationTimestamp
    @Column(name = "flagged_at", updatable = false)
    private LocalDateTime flaggedAt;

    public FlaggedFeedback() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Long feedbackId) { this.feedbackId = feedbackId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }

    public String getCleanedContent() { return cleanedContent; }
    public void setCleanedContent(String cleanedContent) { this.cleanedContent = cleanedContent; }

    public FlagStatus getStatus() { return status; }
    public void setStatus(FlagStatus status) { this.status = status; }

    public LocalDateTime getFlaggedAt() { return flaggedAt; }
    public void setFlaggedAt(LocalDateTime flaggedAt) { this.flaggedAt = flaggedAt; }
}