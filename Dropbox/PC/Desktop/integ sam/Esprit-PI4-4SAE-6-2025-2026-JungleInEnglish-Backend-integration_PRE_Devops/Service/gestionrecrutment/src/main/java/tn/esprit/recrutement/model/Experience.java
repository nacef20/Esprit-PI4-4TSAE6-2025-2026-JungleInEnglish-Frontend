package tn.esprit.recrutement.model;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.recrutement.model.CV;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intitulePost;
    private String entreprise;
    private String duree;
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CV cv;
} 