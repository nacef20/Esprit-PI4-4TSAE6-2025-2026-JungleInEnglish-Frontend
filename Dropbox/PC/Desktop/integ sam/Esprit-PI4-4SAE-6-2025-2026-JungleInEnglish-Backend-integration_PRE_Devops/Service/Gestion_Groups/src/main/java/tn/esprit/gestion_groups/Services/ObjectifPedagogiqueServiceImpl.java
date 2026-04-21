package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_groups.Entites.CompetenceCible;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.ObjectifPedagogique;
import tn.esprit.gestion_groups.Entites.StatutObjectif;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.ObjectifPedagogiqueRepository;
import tn.esprit.gestion_groups.dto.ObjectifSimpleDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ObjectifPedagogiqueServiceImpl implements IObjectifPedagogiqueService {

    final ObjectifPedagogiqueRepository objectifRepository;
    final GroupeRepository groupeRepository;

    @Override
    public ObjectifPedagogique addObjectif(ObjectifPedagogique objectif) {
        return objectifRepository.save(objectif);
    }

    @Override
    public ObjectifPedagogique updateObjectif(ObjectifPedagogique objectif) {
        return objectifRepository.save(objectif);
    }

    @Override
    public List<ObjectifPedagogique> getAllObjectifs() {
        return objectifRepository.findAll();
    }

    @Override
    public List<ObjectifSimpleDTO> getAllObjectifsSimple() {
        return objectifRepository.findAll().stream()
                .map(o -> ObjectifSimpleDTO.builder()
                        .idObjectif(o.getIdObjectif())
                        .codeObjectif(o.getCodeObjectif())
                        .titre(o.getTitre())
                        .description(o.getDescription())
                        .competenceCible(o.getCompetenceCible())
                        .niveauDifficulte(o.getNiveauDifficulte())
                        .priorite(o.getPriorite())
                        .dateDebutPrevue(o.getDateDebutPrevue())
                        .dateFinPrevue(o.getDateFinPrevue())
                        .dureeEstimeeHeures(o.getDureeEstimeeHeures())
                        .statut(o.getStatut())
                        .tauxReussiteGroupe(o.getTauxReussiteGroupe())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ObjectifPedagogique getObjectifById(Long idObjectif) {
        return objectifRepository.findById(idObjectif).orElse(null);
    }

    @Override
    public void deleteObjectif(Long idObjectif) {
        objectifRepository.deleteById(idObjectif);
    }

    @Override
    public ObjectifPedagogique addObjectifAndAssignToGroupe(ObjectifPedagogique objectif, Long groupeId) {
        Groupe groupe = groupeRepository.findById(groupeId).orElse(null);
        if (groupe != null) {
            objectif.setGroupe(groupe);
            return objectifRepository.save(objectif);
        }
        return null;
    }

    @Override
    public void assignObjectifToGroupe(Long objectifId, Long groupeId) {
        ObjectifPedagogique objectif = objectifRepository.findById(objectifId).orElse(null);
        Groupe groupe = groupeRepository.findById(groupeId).orElse(null);

        if (objectif != null && groupe != null) {
            objectif.setGroupe(groupe);
            objectifRepository.save(objectif);
        }
    }

    @Override
    public ObjectifPedagogique desaffecterObjectifFromGroupe(Long objectifId) {
        ObjectifPedagogique objectif = objectifRepository.findById(objectifId).orElse(null);
        if (objectif != null) {
            objectif.setGroupe(null);
            return objectifRepository.save(objectif);
        }
        return null;
    }

    @Override
    public List<ObjectifPedagogique> getObjectifsByGroupe(Long groupeId) {
        return objectifRepository.findByGroupeIdGroupe(groupeId);
    }

    @Override
    public List<ObjectifPedagogique> getObjectifsByStatut(StatutObjectif statut) {
        return objectifRepository.findByStatut(statut);
    }

    @Override
    public List<ObjectifPedagogique> getObjectifsByCompetence(CompetenceCible competence) {
        return objectifRepository.findByCompetenceCible(competence);
    }

    @Override
    public List<ObjectifPedagogique> getObjectifsByGroupeAndStatut(Long groupeId, StatutObjectif statut) {
        return objectifRepository.findByGroupeIdGroupeAndStatut(groupeId, statut);
    }

    @Override
    public List<ObjectifPedagogique> getObjectifsOrderedByPriorite(Long groupeId) {
        return objectifRepository.findByGroupeIdGroupeOrderByPrioriteAsc(groupeId);
    }

    @Override
    public ObjectifPedagogique marquerObjectifAtteint(Long idObjectif) {
        ObjectifPedagogique objectif = objectifRepository.findById(idObjectif).orElse(null);
        if (objectif != null) {
            objectif.setStatut(StatutObjectif.ATTEINT);
            return objectifRepository.save(objectif);
        }
        return null;
    }

    @Override
    public ObjectifPedagogique updateStatutObjectif(Long idObjectif, StatutObjectif nouveauStatut) {
        ObjectifPedagogique objectif = objectifRepository.findById(idObjectif).orElse(null);
        if (objectif != null) {
            objectif.setStatut(nouveauStatut);
            return objectifRepository.save(objectif);
        }
        return null;
    }

    @Override
    public ObjectifPedagogique updateTauxReussite(Long idObjectif, Integer tauxReussite) {
        ObjectifPedagogique objectif = objectifRepository.findById(idObjectif).orElse(null);
        if (objectif != null) {
            objectif.setTauxReussiteGroupe(tauxReussite);
            return objectifRepository.save(objectif);
        }
        return null;
    }
}