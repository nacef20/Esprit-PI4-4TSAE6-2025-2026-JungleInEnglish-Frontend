package tn.esprit.gestion_planning.Entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom_salle;

    private Integer capacite;

    @Enumerated(EnumType.STRING)
    private DisponibiliteSalle disponibilite;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<Equipement> equipements = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}