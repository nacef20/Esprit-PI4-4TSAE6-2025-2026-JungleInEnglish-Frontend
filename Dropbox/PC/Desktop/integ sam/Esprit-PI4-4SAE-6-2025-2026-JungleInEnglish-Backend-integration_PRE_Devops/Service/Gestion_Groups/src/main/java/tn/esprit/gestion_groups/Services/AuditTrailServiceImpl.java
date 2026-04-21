package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_groups.dto.ParcoursEtudiantDTO;
import tn.esprit.gestion_groups.dto.RetentionGroupeDTO;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.GroupeEtudiantRepository;
import tn.esprit.gestion_groups.Repositories.HistoriqueGroupeRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuditTrailServiceImpl implements IAuditTrailService {

    private final GroupeEtudiantRepository groupeEtudiantRepository;
    private final HistoriqueGroupeRepository historiqueRepository;

    // ================================================================
    // PARCOURS COMPLET D'UN ÉTUDIANT
    // ================================================================

    @Override
    public List<ParcoursEtudiantDTO> getParcoursEtudiant(Long etudiantId) {

        // 1. Récupère toutes les affectations de l'étudiant (JOIN FETCH groupe)
        List<GroupeEtudiant> affectations =
                groupeEtudiantRepository.findParcoursCompletEtudiant(etudiantId);

        if (affectations.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Récupère l'historique pour enrichir avec raisons et scores
        List<HistoriqueGroupe> historiques =
                historiqueRepository.findByEtudiantIdOrderByDateChangementAsc(etudiantId);

        // Map : groupeNouveauId → dernier historique
        Map<Long, HistoriqueGroupe> historiqueParGroupe = new HashMap<>();
        for (HistoriqueGroupe h : historiques) {
            if (h.getGroupe() != null) {
                historiqueParGroupe.put(h.getGroupe().getIdGroupe(), h);
            }
        }

        // 3. Construire le parcours
        List<ParcoursEtudiantDTO> parcours = new ArrayList<>();

        for (GroupeEtudiant ge : affectations) {
            Groupe g = ge.getGroupe();
            HistoriqueGroupe hist = historiqueParGroupe.get(g.getIdGroupe());

            // Calcul durée de séjour
            LocalDate dateFin = ge.getDateSortie() != null ? ge.getDateSortie() : LocalDate.now();
            int dureeSejour = (int) ChronoUnit.DAYS.between(ge.getDateAffectation(), dateFin);

            // Calcul évolution score
            Integer scoreApres = (hist != null && hist.getScoreApres() != null)
                    ? hist.getScoreApres() : null;
            int evolutionScore = 0;
            if (scoreApres != null && ge.getScoreEntree() != null) {
                evolutionScore = scoreApres - ge.getScoreEntree();
            }

            String raisonChangement = (hist != null && hist.getRaisonChangement() != null)
                    ? hist.getRaisonChangement().name() : null;

            parcours.add(ParcoursEtudiantDTO.builder()
                    .etudiantId(etudiantId)
                    .idGroupe(g.getIdGroupe())
                    .nomGroupe(g.getNom())
                    .codeGroupe(g.getCodeGroupe())
                    .dateAffectation(ge.getDateAffectation())
                    .dateSortie(ge.getDateSortie())
                    .statut(ge.getStatut().name())
                    .scoreEntree(ge.getScoreEntree())
                    .scoreApres(scoreApres)
                    .raisonChangement(raisonChangement)
                    .commentaire(ge.getCommentaire())
                    .dureeSejour(dureeSejour)
                    .evolutionScore(evolutionScore)
                    .build());
        }

        return parcours;
    }

    // ================================================================
    // STATISTIQUES DE RÉTENTION PAR GROUPE
    // ================================================================

    @Override
    public List<RetentionGroupeDTO> getStatistiquesRetention(long seuilMinimum) {

        // 1. Compteurs par groupe (total, sortis, suspendus, actifs)
        List<Object[]> statsRaw = groupeEtudiantRepository
                .getStatistiquesRetentionParGroupe(seuilMinimum);

        // 2. Durées moyennes par groupe
        List<Object[]> dureesRaw = groupeEtudiantRepository.getDureeMoyenneSejourParGroupe();
        Map<Long, Double> dureeParGroupe = new HashMap<>();
        for (Object[] row : dureesRaw) {
            Long groupeId = (Long) row[0];
            Double duree = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            dureeParGroupe.put(groupeId, duree);
        }

        // 3. Combiner
        return statsRaw.stream()
                .map(row -> {
                    Long idGroupe = (Long) row[0];
                    String nomGroupe = (String) row[1];
                    long total = ((Number) row[2]).longValue();
                    long sortis = ((Number) row[3]).longValue();
                    long suspendus = ((Number) row[4]).longValue();
                    long actifs = ((Number) row[5]).longValue();
                    double dureeMoyenne = dureeParGroupe.getOrDefault(idGroupe, 0.0);
                    double tauxRetention = total > 0
                            ? Math.round(((double) actifs / total) * 1000.0) / 10.0
                            : 0.0;

                    return RetentionGroupeDTO.builder()
                            .idGroupe(idGroupe)
                            .nomGroupe(nomGroupe)
                            .totalAffectations(total)
                            .dureeMoyenneJours(Math.round(dureeMoyenne * 10.0) / 10.0)
                            .nombreSortis(sortis)
                            .nombreSuspendus(suspendus)
                            .nombreActifs(actifs)
                            .tauxRetention(tauxRetention)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
