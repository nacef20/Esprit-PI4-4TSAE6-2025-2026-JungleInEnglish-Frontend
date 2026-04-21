package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.dto.FusionGroupeRequest;
import tn.esprit.gestion_groups.dto.FusionResultatDTO;
import tn.esprit.gestion_groups.Services.IFusionGroupeService;

@RestController
@AllArgsConstructor
@RequestMapping("/fusion")
public class FusionGroupeController {

    private final IFusionGroupeService fusionService;

    /**
     * POST /fusion/valider
     *
     * Simule la fusion SANS l'exécuter.
     * Retourne un rapport de faisabilité :
     *  - combien d'étudiants peuvent être transférés
     *  - combien ne peuvent pas (groupe cible trop plein)
     *
     * À appeler depuis le frontend AVANT de confirmer la fusion.
     *
     * Body JSON :
     * {
     *   "groupeSourceId": 3,
     *   "groupeCibleId": 7,
     *   "transfererEtudiants": true,
     *   "transfererFormateurs": false,
     *   "transfererObjectifs": false,
     *   "transfererReglements": false,
     *   "commentaire": "Effectif insuffisant",
     *   "declenchePar": 1
     * }
     */
    @PostMapping("/valider")
    public FusionResultatDTO validerFusion(@RequestBody FusionGroupeRequest request) {
        return fusionService.validerFusion(request);
    }

    /**
     * POST /fusion/executer
     *
     * Exécute la fusion réelle — IRRÉVERSIBLE.
     * Recommander d'appeler /fusion/valider avant.
     *
     * Ce que ça fait dans l'ordre :
     *  1. Valide que les deux groupes sont fusionnables
     *  2. Transfère les étudiants ACTIF → crée un HistoriqueGroupe par étudiant
     *  3. Transfère les formateurs si transfererFormateurs=true
     *  4. Rattache les objectifs si transfererObjectifs=true
     *  5. Rattache les règlements si transfererReglements=true
     *  6. Archive le groupe source (statut ARCHIVE, capacite=0)
     *  7. Retourne le rapport complet de ce qui a été fait
     */
    @PostMapping("/executer")
    public FusionResultatDTO executerFusion(@RequestBody FusionGroupeRequest request) {
        return fusionService.fusionnerGroupes(request);
    }
}
