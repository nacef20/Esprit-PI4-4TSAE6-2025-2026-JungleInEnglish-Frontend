package com.fluently.feedbackservice.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_reactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"feedback_id", "reactor_student_id"})
})
public class FeedbackReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Column(name = "reactor_student_id", nullable = false)
    private String reactorStudentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public FeedbackReaction() {}

    public FeedbackReaction(Long id, Feedback feedback, String reactorStudentId,
                            ReactionType reactionType, LocalDateTime createdAt) {
        this.id = id;
        this.feedback = feedback;
        this.reactorStudentId = reactorStudentId;
        this.reactionType = reactionType;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }

    public String getReactorStudentId() { return reactorStudentId; }
    public void setReactorStudentId(String reactorStudentId) { this.reactorStudentId = reactorStudentId; }

    public ReactionType getReactionType() { return reactionType; }
    public void setReactionType(ReactionType reactionType) { this.reactionType = reactionType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}