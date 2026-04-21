package tn.esprit.gestion_planning.DTO;
// SemesterReplicationRequest.java

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemesterReplicationRequest {

    // La semaine de référence (lundi de cette semaine)
    private LocalDate semaineReference;

    // Date de début du semestre
    private LocalDate debutSemestre;

    // Date de fin du semestre
    private LocalDate finSemestre;

    // Dates des jours fériés à exclure
    private List<LocalDate> joursFeries;

    // Lundis des semaines d'examens à bloquer
    private List<LocalDate> semainesExamens;
}