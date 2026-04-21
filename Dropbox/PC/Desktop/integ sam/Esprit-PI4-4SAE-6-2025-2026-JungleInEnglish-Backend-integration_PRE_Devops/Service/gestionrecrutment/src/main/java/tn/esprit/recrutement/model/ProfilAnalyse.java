package tn.esprit.recrutement.model;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.recrutement.model.CV;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfilAnalyse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "cv_id")
    private CV cv;

    @Column(columnDefinition = "TEXT")
    private String resume;
} 