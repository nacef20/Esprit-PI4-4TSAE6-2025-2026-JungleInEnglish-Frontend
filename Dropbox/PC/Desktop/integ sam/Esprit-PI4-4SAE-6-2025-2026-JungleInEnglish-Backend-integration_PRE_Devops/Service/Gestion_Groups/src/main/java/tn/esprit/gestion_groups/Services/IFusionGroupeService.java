package tn.esprit.gestion_groups.Services;

import tn.esprit.gestion_groups.dto.FusionGroupeRequest;
import tn.esprit.gestion_groups.dto.FusionResultatDTO;

public interface IFusionGroupeService {

    /**
     * Exécute la fusion complète d'un groupe source vers un groupe cible.
     *
     * Étapes dans l'ordre :
     *  1. Validation : les deux groupes existent et sont fusionnables
     *  2. Vérifie que le groupe cible peut accueillir les étudiants
     *  3. Transfère les étudiants ACTIF (crée HistoriqueGroupe pour chacun)
     *  4. Transfère les formateurs si demandé (skip si rôle déjà pris)
     *  5. Rattache les objectifs au groupe cible si demandé
     *  6. Rattache les règlements au groupe cible si demandé
     *  7. Archive le groupe source (statut = ARCHIVE, capaciteActuelle = 0)
     *  8. Retourne un rapport détaillé de ce qui a été fait
     */
    FusionResultatDTO fusionnerGroupes(FusionGroupeRequest request);

    /**
     * Valide si une fusion est possible SANS l'exécuter.
     * Retourne un rapport de faisabilité.
     */
    FusionResultatDTO validerFusion(FusionGroupeRequest request);
}
