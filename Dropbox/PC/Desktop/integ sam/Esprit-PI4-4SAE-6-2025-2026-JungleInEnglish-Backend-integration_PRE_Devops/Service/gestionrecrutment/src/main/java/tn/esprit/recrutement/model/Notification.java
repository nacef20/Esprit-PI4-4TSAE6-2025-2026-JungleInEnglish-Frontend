package tn.esprit.recrutement.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String candidateFullName;

    @Column(nullable = false)
    private String jobOfferTitle;

    @Column(nullable = false)
    private Double matchingScore;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date applicationDate;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    /**
     * EMPLOYER = notification for the job offer's employer only.
     * ADMIN = notification for any admin.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecipientRole recipientRole;

    /**
     * Set when recipientRole is EMPLOYER: id of the employer who owns the job offer.
     */
    private Integer employerId;

    /**
     * ID of the job offer this notification refers to (for navigation).
     */
    private Long jobOfferId;
}
