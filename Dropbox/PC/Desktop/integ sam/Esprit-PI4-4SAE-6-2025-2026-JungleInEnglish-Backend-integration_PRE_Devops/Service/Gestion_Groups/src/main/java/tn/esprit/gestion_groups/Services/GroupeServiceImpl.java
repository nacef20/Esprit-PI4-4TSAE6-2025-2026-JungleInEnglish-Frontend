package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_groups.Entites.GroupeFormateur;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.GroupeFormateurRepository;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.StatutGroupe;
import tn.esprit.gestion_groups.Entites.TypeGroupe;
import tn.esprit.gestion_groups.clients.UserFeignClient;
import tn.esprit.gestion_groups.dto.FormateurEnrichiDTO;
import tn.esprit.gestion_groups.dto.GroupeEnrichiDTO;
import tn.esprit.gestion_groups.dto.GroupeSimpleDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class GroupeServiceImpl implements IGroupeService {

    final GroupeRepository groupeRepository;
    final GroupeFormateurRepository groupeFormateurRepository;
    final UserFeignClient userFeignClient;

    @Override
    public Groupe addGroupe(Groupe groupe) {
        return groupeRepository.save(groupe);
    }

    @Override
    public Groupe updateGroupe(Groupe groupe) {
        return groupeRepository.save(groupe);
    }

    @Override
    public List<Groupe> getAllGroupes() {
        return groupeRepository.findAll();
    }

    @Override
    public List<GroupeSimpleDTO> getAllGroupesSimple() {
        return groupeRepository.findAll().stream()
                .map(g -> GroupeSimpleDTO.builder()
                        .idGroupe(g.getIdGroupe())
                        .codeGroupe(g.getCodeGroupe())
                        .nom(g.getNom())
                        .niveauId(g.getNiveauId())
                        .capaciteMin(g.getCapaciteMin())
                        .capaciteMax(g.getCapaciteMax())
                        .capaciteActuelle(g.getCapaciteActuelle())
                        .dateDebut(g.getDateDebut())
                        .dateFin(g.getDateFin())
                        .type(g.getType())
                        .statut(g.getStatut())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Groupe getGroupeById(Long idGroupe) {
        return groupeRepository.findById(idGroupe).orElse(null);
    }

    @Override
    public Groupe getGroupeWithDetails(Long idGroupe) {
        return groupeRepository.findByIdWithDetails(idGroupe).orElse(null);
    }

    @Override
    public GroupeEnrichiDTO getGroupeEnrichi(Long idGroupe) {
        Groupe groupe = groupeRepository.findByIdWithDetails(idGroupe).orElse(null);
        if (groupe == null) {
            return null;
        }

        List<GroupeFormateur> formateurs = groupeFormateurRepository.findFormateursActifsByGroupe(idGroupe);

        return GroupeEnrichiDTO.builder()
                .idGroupe(groupe.getIdGroupe())
                .codeGroupe(groupe.getCodeGroupe())
                .nom(groupe.getNom())
                .niveauId(groupe.getNiveauId())
                .capaciteMin(groupe.getCapaciteMin())
                .capaciteMax(groupe.getCapaciteMax())
                .capaciteActuelle(groupe.getCapaciteActuelle())
                .dateDebut(groupe.getDateDebut())
                .dateFin(groupe.getDateFin())
                .type(groupe.getType())
                .statut(groupe.getStatut())
                .createdAt(groupe.getCreatedAt())
                .updatedAt(groupe.getUpdatedAt())
                .nombreFormateursActifs(formateurs.size())
                .formateurs(formateurs.stream().map(this::toFormateurEnrichiDTO).collect(Collectors.toList()))
                .build();
    }

    @Override
    public void deleteGroupe(Long idGroupe) {
        groupeRepository.deleteById(idGroupe);
    }

    @Override
    public Groupe getGroupeByCode(String codeGroupe) {
        return groupeRepository.findByCodeGroupe(codeGroupe).orElse(null);
    }

    @Override
    public List<Groupe> getGroupesByStatut(StatutGroupe statut) {
        return groupeRepository.findByStatut(statut);
    }

    @Override
    public List<Groupe> getGroupesByType(TypeGroupe type) {
        return groupeRepository.findByType(type);
    }

    @Override
    public List<Groupe> getGroupesByNiveau(Long niveauId) {
        return groupeRepository.findByNiveauId(niveauId);
    }

    @Override
    public List<Groupe> getGroupesDisponibles() {
        // CORRECTION : Appel de la nouvelle méthode
        return groupeRepository.findGroupesDisponibles();
    }

    @Override
    public List<Groupe> searchGroupesByNom(String nom) {
        return groupeRepository.findByNomContainingIgnoreCase(nom);
    }

    @Override
    public Groupe updateStatutGroupe(Long idGroupe, StatutGroupe nouveauStatut) {
        Groupe groupe = groupeRepository.findById(idGroupe).orElse(null);
        if (groupe != null) {
            groupe.setStatut(nouveauStatut);
            return groupeRepository.save(groupe);
        }
        return null;
    }

    @Override
    public Groupe incrementerCapacite(Long idGroupe) {
        Groupe groupe = groupeRepository.findById(idGroupe).orElse(null);
        if (groupe != null) {
            groupe.setCapaciteActuelle(groupe.getCapaciteActuelle() + 1);

            // Auto-update statut si capacité max atteinte
            if (groupe.getCapaciteActuelle().equals(groupe.getCapaciteMax())) {
                groupe.setStatut(StatutGroupe.COMPLET);
            }

            return groupeRepository.save(groupe);
        }
        return null;
    }

    @Override
    public Groupe decrementerCapacite(Long idGroupe) {
        Groupe groupe = groupeRepository.findById(idGroupe).orElse(null);
        if (groupe != null && groupe.getCapaciteActuelle() > 0) {
            groupe.setCapaciteActuelle(groupe.getCapaciteActuelle() - 1);

            // Auto-update statut si n'est plus complet
            if (groupe.getStatut() == StatutGroupe.COMPLET) {
                groupe.setStatut(StatutGroupe.OUVERT);
            }

            return groupeRepository.save(groupe);
        }
        return null;
    }

    private FormateurEnrichiDTO toFormateurEnrichiDTO(GroupeFormateur groupeFormateur) {
        UserFeignClient.UtilisateurResumeDTO utilisateur = null;

        try {
            utilisateur = userFeignClient.getUserById(String.valueOf(groupeFormateur.getFormateurId()));
        } catch (Exception exception) {
            log.warn("Impossible de récupérer l'utilisateur {} depuis userservice: {}",
                    groupeFormateur.getFormateurId(), exception.getMessage());
        }

        return FormateurEnrichiDTO.builder()
                .formateurId(groupeFormateur.getFormateurId())
                .username(utilisateur != null ? utilisateur.getUsername() : null)
                .firstName(utilisateur != null ? utilisateur.getFirstName() : null)
                .lastName(utilisateur != null ? utilisateur.getLastName() : null)
                .email(utilisateur != null ? utilisateur.getEmail() : null)
                .role(groupeFormateur.getRole())
                .dateDebut(groupeFormateur.getDateDebut())
                .dateFin(groupeFormateur.getDateFin())
                .estActif(groupeFormateur.getEstActif())
                .matiere(groupeFormateur.getMatiere())
                .heuresParSemaine(groupeFormateur.getHeuresParSemaine())
                .commentaire(groupeFormateur.getCommentaire())
                .build();
    }
}