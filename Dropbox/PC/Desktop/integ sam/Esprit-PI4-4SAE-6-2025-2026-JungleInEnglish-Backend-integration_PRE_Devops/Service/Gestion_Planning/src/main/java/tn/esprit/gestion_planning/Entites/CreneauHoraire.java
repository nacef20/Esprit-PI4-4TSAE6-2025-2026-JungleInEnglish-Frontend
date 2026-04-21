package tn.esprit.gestion_planning.Entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreneauHoraire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCreneauHoraire;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    @Enumerated(EnumType.STRING)
    private TypeCreneau type;

    @Enumerated(EnumType.STRING)
    private JourSemaine jourSemaine;
}
