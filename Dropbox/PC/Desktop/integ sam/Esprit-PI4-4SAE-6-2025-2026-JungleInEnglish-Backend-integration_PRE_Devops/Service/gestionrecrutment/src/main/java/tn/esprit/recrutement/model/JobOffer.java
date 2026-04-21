package tn.esprit.recrutement.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private User employer;

    private String title;
    private String description;
    private String location;
    private String employmentType;
    private String experienceLevel;
    private Double salary;
    private String currency;
    private Date postingDate;
    private Date closingDate;
    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @ElementCollection
    @CollectionTable(name = "job_skills")
    private Set<String> requiredSkills;

    @ElementCollection
    @CollectionTable(name = "job_qualifications")
    private Set<String> requiredQualifications;

    @ElementCollection
    @CollectionTable(name = "job_languages")
    private Set<String> requiredLanguages;

    @ElementCollection
    @CollectionTable(name = "job_certifications")
    private Set<String> requiredCertifications;
}
