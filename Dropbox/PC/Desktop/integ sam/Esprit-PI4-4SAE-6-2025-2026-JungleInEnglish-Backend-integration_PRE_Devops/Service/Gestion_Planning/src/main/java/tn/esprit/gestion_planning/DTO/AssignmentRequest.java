package tn.esprit.gestion_planning.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
    private Long groupeId;
    private Long matiereId;
    private Long enseignantId;
}