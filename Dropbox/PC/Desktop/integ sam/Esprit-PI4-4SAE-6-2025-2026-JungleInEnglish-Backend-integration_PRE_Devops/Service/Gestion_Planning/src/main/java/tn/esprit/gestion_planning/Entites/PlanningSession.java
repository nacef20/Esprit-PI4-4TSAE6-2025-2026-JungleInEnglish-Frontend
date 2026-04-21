package tn.esprit.gestion_planning.Entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanningSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlanningSession;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salle_id", nullable = true)   // ✅ changer false → true
    private Salle salle;
    @ManyToOne(fetch = FetchType.EAGER)
    private CreneauHoraire creneau;

    private Long groupeId;

    private Long enseignantId;

    private Long matiereId;

    private LocalDate datePlanning;

    @Enumerated(EnumType.STRING)
    private StatutPlanning statut = StatutPlanning.PLANIFIE;

    private String observations;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
