package tn.esprit.gestion_groups.Services;


import tn.esprit.gestion_groups.Entites.CategorieReglement;
import tn.esprit.gestion_groups.Entites.ReglementGroupe;
import tn.esprit.gestion_groups.dto.ReglementSimpleDTO;

import java.util.List;

public interface IReglementGroupeService {
    ReglementGroupe addReglement(ReglementGroupe reglement);
    ReglementGroupe updateReglement(ReglementGroupe reglement);
    List<ReglementGroupe> getAllReglements();
    List<ReglementSimpleDTO> getAllReglementsSimple();
    ReglementGroupe getReglementById(Long idReglement);
    void deleteReglement(Long idReglement);

    ReglementGroupe addReglementAndAssignToGroupe(ReglementGroupe reglement, Long groupeId);
    void assignReglementToGroupe(Long reglementId, Long groupeId);
    ReglementGroupe desaffecterReglementFromGroupe(Long reglementId);

    List<ReglementGroupe> getReglementsByGroupe(Long groupeId);
    List<ReglementGroupe> getReglementsByCategorie(CategorieReglement categorie);
    List<ReglementGroupe> getReglementsActifs();
    List<ReglementGroupe> getReglementsActifsByGroupe(Long groupeId);

    ReglementGroupe activerReglement(Long idReglement);
    ReglementGroupe desactiverReglement(Long idReglement);
}