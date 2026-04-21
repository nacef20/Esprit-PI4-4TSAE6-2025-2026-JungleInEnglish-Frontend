package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.StatutObjectif;
import tn.esprit.gestion_groups.Repositories.HistoriqueGroupeRepository;
import tn.esprit.gestion_groups.Repositories.ObjectifPedagogiqueRepository;
import tn.esprit.gestion_groups.Repositories.ReglementGroupeRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class GroupeHealthScoreService {

    private final ObjectifPedagogiqueRepository objectifRepository;
    private final ReglementGroupeRepository reglementRepository;
    private final HistoriqueGroupeRepository historiqueRepository;

    public Integer calculerScoreTotal(Groupe groupe) {
        if (groupe == null) {
            return null;
        }

        int capaciteActuelle = groupe.getCapaciteActuelle() != null ? groupe.getCapaciteActuelle() : 0;
        int capaciteMax = groupe.getCapaciteMax() != null ? groupe.getCapaciteMax() : 0;

        long objectifsAtteints = objectifRepository.findByGroupeIdGroupeAndStatut(groupe.getIdGroupe(), StatutObjectif.ATTEINT).size();
        long totalObjectifs = objectifRepository.findByGroupeIdGroupe(groupe.getIdGroupe()).size();
        long reglementsObligatoiresActifs = reglementRepository.findByGroupeIdGroupeAndEstActif(groupe.getIdGroupe(), true).stream()
                .filter(reglement -> Boolean.TRUE.equals(reglement.getEstObligatoire()))
                .count();
        long totalReglementsObligatoires = reglementRepository.findByGroupeIdGroupe(groupe.getIdGroupe()).stream()
                .filter(reglement -> Boolean.TRUE.equals(reglement.getEstObligatoire()))
                .count();
        long changementsRecents = historiqueRepository.countChangementsRecentsByGroupe(
                groupe.getIdGroupe(),
                LocalDateTime.now().minusDays(30)
        );

        double scoreTaux = capaciteMax > 0
                ? Math.min(25.0, ((double) capaciteActuelle / capaciteMax) * 25)
                : 0.0;
        double scoreObj = totalObjectifs > 0
                ? ((double) objectifsAtteints / totalObjectifs) * 25
                : 12.5;
        double scoreReg = totalReglementsObligatoires > 0
                ? ((double) reglementsObligatoiresActifs / totalReglementsObligatoires) * 25
                : 12.5;
        double scoreStab = Math.max(0.0, 25.0 - (changementsRecents * 5.0));

        return (int) Math.round(scoreTaux + scoreObj + scoreReg + scoreStab);
    }
}