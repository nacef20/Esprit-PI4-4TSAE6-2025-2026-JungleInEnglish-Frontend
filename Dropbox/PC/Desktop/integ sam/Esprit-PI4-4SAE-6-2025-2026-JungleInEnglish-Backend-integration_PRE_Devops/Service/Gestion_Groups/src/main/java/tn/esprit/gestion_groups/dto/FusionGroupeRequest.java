package tn.esprit.gestion_groups.dto;

import lombok.Getter;
import lombok.Setter;

// ── Request : paramètres de la fusion ───────────────────────────────
@Getter
@Setter
public class FusionGroupeRequest {

    private Long groupeSourceId;       // groupe à vider + archiver
    private Long groupeCibleId;        // groupe qui reçoit tout

    private Boolean transfererEtudiants  = true;   // déplacer les étudiants ACTIF
    private Boolean transfererFormateurs = false;   // déplacer les formateurs actifs
    private Boolean transfererObjectifs  = false;   // rattacher les objectifs au groupe cible
    private Boolean transfererReglements = false;   // rattacher les règlements au groupe cible

    private String commentaire;        // raison de la fusion (affiché dans HistoriqueGroupe)
    private Long declenchePar;         // user_id admin qui déclenche
}
