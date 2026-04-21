package tn.esprit.gestion_groups.Services;


import tn.esprit.gestion_groups.Entites.CompetenceCible;
import tn.esprit.gestion_groups.Entites.ObjectifPedagogique;
import tn.esprit.gestion_groups.Entites.StatutObjectif;
import tn.esprit.gestion_groups.dto.ObjectifSimpleDTO;

import java.util.List;

public interface IObjectifPedagogiqueService {
    ObjectifPedagogique addObjectif(ObjectifPedagogique objectif);
    ObjectifPedagogique updateObjectif(ObjectifPedagogique objectif);
    List<ObjectifPedagogique> getAllObjectifs();
    List<ObjectifSimpleDTO> getAllObjectifsSimple();
    ObjectifPedagogique getObjectifById(Long idObjectif);
    void deleteObjectif(Long idObjectif);

    ObjectifPedagogique addObjectifAndAssignToGroupe(ObjectifPedagogique objectif, Long groupeId);
    void assignObjectifToGroupe(Long objectifId, Long groupeId);
    ObjectifPedagogique desaffecterObjectifFromGroupe(Long objectifId);

    List<ObjectifPedagogique> getObjectifsByGroupe(Long groupeId);
    List<ObjectifPedagogique> getObjectifsByStatut(StatutObjectif statut);
    List<ObjectifPedagogique> getObjectifsByCompetence(CompetenceCible competence);
    List<ObjectifPedagogique> getObjectifsByGroupeAndStatut(Long groupeId, StatutObjectif statut);
    List<ObjectifPedagogique> getObjectifsOrderedByPriorite(Long groupeId);

    ObjectifPedagogique marquerObjectifAtteint(Long idObjectif);
    ObjectifPedagogique updateStatutObjectif(Long idObjectif, StatutObjectif nouveauStatut);
    ObjectifPedagogique updateTauxReussite(Long idObjectif, Integer tauxReussite);
}