package tn.esprit.recrutement.model;

import tn.esprit.recrutement.model.JobOffer;
import tn.esprit.recrutement.model.CV;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CVMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CV cv;

    @ManyToOne
    @JoinColumn(name = "job_offer_id")
    private JobOffer jobOffer;

    private double matchScore;
    private int skillsMatch;
    private int experienceMatch;
    private int educationMatch;
    private int languageMatch;
    private int certificationMatch;
    private Date matchDate;
    private String status = "PENDING";
}
