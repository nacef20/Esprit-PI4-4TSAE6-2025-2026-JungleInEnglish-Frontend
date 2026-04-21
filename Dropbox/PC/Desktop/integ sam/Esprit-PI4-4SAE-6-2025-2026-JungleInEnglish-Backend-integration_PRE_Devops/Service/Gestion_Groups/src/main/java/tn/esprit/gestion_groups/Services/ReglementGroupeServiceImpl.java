package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_groups.Entites.CategorieReglement;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.ReglementGroupe;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.ReglementGroupeRepository;
import tn.esprit.gestion_groups.dto.ReglementSimpleDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReglementGroupeServiceImpl implements IReglementGroupeService {

    final ReglementGroupeRepository reglementRepository;
    final GroupeRepository groupeRepository;

    @Override
    public ReglementGroupe addReglement(ReglementGroupe reglement) {
        return reglementRepository.save(reglement);
    }

    @Override
    public ReglementGroupe updateReglement(ReglementGroupe reglement) {
        return reglementRepository.save(reglement);
    }

    @Override
    public List<ReglementGroupe> getAllReglements() {
        return reglementRepository.findAll();
    }

    @Override
    public List<ReglementSimpleDTO> getAllReglementsSimple() {
        return reglementRepository.findAll().stream()
                .map(r -> ReglementSimpleDTO.builder()
                        .idReglement(r.getIdReglement())
                        .codeReglement(r.getCodeReglement())
                        .titre(r.getTitre())
                        .categorie(r.getCategorie())
                        .descriptionRegle(r.getDescriptionRegle())
                        .estObligatoire(r.getEstObligatoire())
                        .tolerance(r.getTolerance())
                        .sanction(r.getSanction())
                        .estActif(r.getEstActif())
                        .dateApplication(r.getDateApplication())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ReglementGroupe getReglementById(Long idReglement) {
        return reglementRepository.findById(idReglement).orElse(null);
    }

    @Override
    public void deleteReglement(Long idReglement) {
        reglementRepository.deleteById(idReglement);
    }

    @Override
    public ReglementGroupe addReglementAndAssignToGroupe(ReglementGroupe reglement, Long groupeId) {
        Groupe groupe = groupeRepository.findById(groupeId).orElse(null);
        if (groupe != null) {
            reglement.setGroupe(groupe);
            return reglementRepository.save(reglement);
        }
        return null;
    }

    @Override
    public void assignReglementToGroupe(Long reglementId, Long groupeId) {
        ReglementGroupe reglement = reglementRepository.findById(reglementId).orElse(null);
        Groupe groupe = groupeRepository.findById(groupeId).orElse(null);

        if (reglement != null && groupe != null) {
            reglement.setGroupe(groupe);
            reglementRepository.save(reglement);
        }
    }

    @Override
    public ReglementGroupe desaffecterReglementFromGroupe(Long reglementId) {
        ReglementGroupe reglement = reglementRepository.findById(reglementId).orElse(null);
        if (reglement != null) {
            reglement.setGroupe(null);
            return reglementRepository.save(reglement);
        }
        return null;
    }

    @Override
    public List<ReglementGroupe> getReglementsByGroupe(Long groupeId) {
        return reglementRepository.findByGroupeIdGroupe(groupeId);
    }

    @Override
    public List<ReglementGroupe> getReglementsByCategorie(CategorieReglement categorie) {
        return reglementRepository.findByCategorie(categorie);
    }

    @Override
    public List<ReglementGroupe> getReglementsActifs() {
        return reglementRepository.findByEstActif(true);
    }

    @Override
    public List<ReglementGroupe> getReglementsActifsByGroupe(Long groupeId) {
        return reglementRepository.findByGroupeIdGroupeAndEstActif(groupeId, true);
    }

    @Override
    public ReglementGroupe activerReglement(Long idReglement) {
        ReglementGroupe reglement = reglementRepository.findById(idReglement).orElse(null);
        if (reglement != null) {
            reglement.setEstActif(true);
            return reglementRepository.save(reglement);
        }
        return null;
    }

    @Override
    public ReglementGroupe desactiverReglement(Long idReglement) {
        ReglementGroupe reglement = reglementRepository.findById(idReglement).orElse(null);
        if (reglement != null) {
            reglement.setEstActif(false);
            return reglementRepository.save(reglement);
        }
        return null;
    }
}