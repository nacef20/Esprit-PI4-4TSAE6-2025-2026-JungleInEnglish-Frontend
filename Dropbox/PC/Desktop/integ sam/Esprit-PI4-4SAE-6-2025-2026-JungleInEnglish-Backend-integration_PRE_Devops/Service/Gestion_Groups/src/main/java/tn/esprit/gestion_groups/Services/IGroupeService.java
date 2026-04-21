package tn.esprit.gestion_groups.Services;


import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.StatutGroupe;
import tn.esprit.gestion_groups.Entites.TypeGroupe;
import tn.esprit.gestion_groups.dto.GroupeEnrichiDTO;
import tn.esprit.gestion_groups.dto.GroupeSimpleDTO;

import java.util.List;

public interface IGroupeService {
    Groupe addGroupe(Groupe groupe);
    Groupe updateGroupe(Groupe groupe);
    List<Groupe> getAllGroupes();
    List<GroupeSimpleDTO> getAllGroupesSimple();
    Groupe getGroupeById(Long idGroupe);
    Groupe getGroupeWithDetails(Long idGroupe);
    GroupeEnrichiDTO getGroupeEnrichi(Long idGroupe);
    void deleteGroupe(Long idGroupe);

    Groupe getGroupeByCode(String codeGroupe);
    List<Groupe> getGroupesByStatut(StatutGroupe statut);
    List<Groupe> getGroupesByType(TypeGroupe type);
    List<Groupe> getGroupesByNiveau(Long niveauId);
    List<Groupe> getGroupesDisponibles();
    List<Groupe> searchGroupesByNom(String nom);

    Groupe updateStatutGroupe(Long idGroupe, StatutGroupe nouveauStatut);
    Groupe incrementerCapacite(Long idGroupe);
    Groupe decrementerCapacite(Long idGroupe);
}