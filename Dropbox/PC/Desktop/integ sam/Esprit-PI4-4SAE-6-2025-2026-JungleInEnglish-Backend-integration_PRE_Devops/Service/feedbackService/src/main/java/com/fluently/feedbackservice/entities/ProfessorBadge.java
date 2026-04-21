package com.fluently.feedbackservice.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "professor_badges")
public class ProfessorBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professor_id", nullable = false, unique = true)
    private String professorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false)
    private BadgeType badgeType;

    @CreationTimestamp
    @Column(name = "awarded_at", updatable = false)
    private LocalDateTime awardedAt;

    @Column(name = "star_badge")
    private String starBadge;

    public ProfessorBadge() {}

    public ProfessorBadge(Long id, String professorId, BadgeType badgeType, LocalDateTime awardedAt) {
        this.id = id;
        this.professorId = professorId;
        this.badgeType = badgeType;
        this.awardedAt = awardedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }

    public BadgeType getBadgeType() { return badgeType; }
    public void setBadgeType(BadgeType badgeType) { this.badgeType = badgeType; }

    public LocalDateTime getAwardedAt() { return awardedAt; }
    public void setAwardedAt(LocalDateTime awardedAt) { this.awardedAt = awardedAt; }

    public String getStarBadge() { return starBadge; }
    public void setStarBadge(String starBadge) { this.starBadge = starBadge; }
}