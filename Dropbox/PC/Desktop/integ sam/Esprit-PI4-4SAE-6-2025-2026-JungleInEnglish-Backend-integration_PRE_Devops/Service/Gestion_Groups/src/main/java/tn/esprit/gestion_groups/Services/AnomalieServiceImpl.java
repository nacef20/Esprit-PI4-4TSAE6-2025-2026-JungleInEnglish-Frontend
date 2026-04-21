package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_groups.dto.AlerteGroupeDTO;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.ObjectifPedagogiqueRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AnomalieServiceImpl implements IAnomalieService {

    private final GroupeRepository groupeRepository;
    private final ObjectifPedagogiqueRepository objectifRepository;

    private static final List<StatutGroupe> STATUTS_EXCLUS =
            Arrays.asList(StatutGroupe.ANNULE, StatutGroupe.TERMINE, StatutGroupe.ARCHIVE);

    // ================================================================
    // SCAN COMPLET
    // ================================================================

    @Override
    public List<AlerteGroupeDTO> detecterToutesAnomalies() {

        List<AlerteGroupeDTO> alertes = new ArrayList<>();

        // ── Anomalie CRITIQUE : Groupes sans formateur ──────────────
        List<Groupe> sansFormateur = groupeRepository.findGroupesSansFormateur(STATUTS_EXCLUS);
        for (Groupe g : sansFormateur) {
            alertes.add(AlerteGroupeDTO.builder()
                    .idGroupe(g.getIdGroupe())
                    .nomGroupe(g.getNom())
                    .codeGroupe(g.getCodeGroupe())
                    .typeAnomalie("SANS_FORMATEUR")
                    .severite("CRITIQUE")
                    .priorite(1)
                    .description(String.format(
                            "Le groupe %s (%s) n'a aucun formateur actif assigné. " +
                            "Les %d étudiant(s) n'ont pas d'enseignant.",
                            g.getNom(), g.getCodeGroupe(),
                            g.getCapaciteActuelle() != null ? g.getCapaciteActuelle() : 0))
                    .suggestion("Assigner un formateur PRINCIPAL au groupe depuis le module Affectations.")
                    .build());
        }

        // ── Anomalie CRITIQUE : Capacité incohérente ────────────────
        List<Groupe> incoherents = groupeRepository.findGroupesCapaciteIncoherente(STATUTS_EXCLUS);
        for (Groupe g : incoherents) {
            alertes.add(AlerteGroupeDTO.builder()
                    .idGroupe(g.getIdGroupe())
                    .nomGroupe(g.getNom())
                    .codeGroupe(g.getCodeGroupe())
                    .typeAnomalie("CAPACITE_INCOHERENTE")
                    .severite("CRITIQUE")
                    .priorite(1)
                    .description(String.format(
                            "Le groupe %s a une capacité actuelle (%d) supérieure " +
                            "à sa capacité maximale (%d). Données incohérentes.",
                            g.getCodeGroupe(),
                            g.getCapaciteActuelle(), g.getCapaciteMax()))
                    .suggestion("Vérifier et corriger la capacité actuelle ou maximale du groupe.")
                    .build());
        }

        // ── Anomalie WARNING/INFO : Objectifs en retard ─────────────
        List<ObjectifPedagogique> objectifsRetard = objectifRepository
                .findObjectifsEnRetard(StatutObjectif.EN_COURS, LocalDate.now());

        for (ObjectifPedagogique o : objectifsRetard) {
            long joursRetard = ChronoUnit.DAYS.between(o.getDateFinPrevue(), LocalDate.now());
            String severite = joursRetard > 7 ? "WARNING" : "INFO";
            int priorite = joursRetard > 7 ? 2 : 3;

            Groupe g = o.getGroupe();
            alertes.add(AlerteGroupeDTO.builder()
                    .idGroupe(g.getIdGroupe())
                    .nomGroupe(g.getNom())
                    .codeGroupe(g.getCodeGroupe())
                    .typeAnomalie("OBJECTIF_EN_RETARD")
                    .severite(severite)
                    .priorite(priorite)
                    .description(String.format(
                            "L'objectif \"%s\" (%s) du groupe %s est en retard de %d jour(s). " +
                            "Date fin prévue : %s. Compétence cible : %s.",
                            o.getTitre(), o.getCodeObjectif(),
                            g.getCodeGroupe(), joursRetard,
                            o.getDateFinPrevue(),
                            o.getCompetenceCible() != null ? o.getCompetenceCible() : "N/A"))
                    .suggestion("Mettre à jour le statut de l'objectif ou repousser la date de fin.")
                    .objectifId(o.getIdObjectif())
                    .codeObjectif(o.getCodeObjectif())
                    .joursRetard((int) joursRetard)
                    .build());
        }

        // ── Anomalie INFO : Groupes sans objectif ───────────────────
        List<Groupe> sansObjectif = groupeRepository.findGroupesSansObjectif(STATUTS_EXCLUS);
        for (Groupe g : sansObjectif) {
            alertes.add(AlerteGroupeDTO.builder()
                    .idGroupe(g.getIdGroupe())
                    .nomGroupe(g.getNom())
                    .codeGroupe(g.getCodeGroupe())
                    .typeAnomalie("SANS_OBJECTIF")
                    .severite("INFO")
                    .priorite(3)
                    .description(String.format(
                            "Le groupe %s (%s) n'a aucun objectif pédagogique défini. " +
                            "Impossible de mesurer la progression.",
                            g.getNom(), g.getCodeGroupe()))
                    .suggestion("Définir des objectifs pédagogiques pour ce groupe.")
                    .build());
        }

        // Tri par priorité (CRITIQUE d'abord) puis par nom de groupe
        alertes.sort(Comparator.comparingInt(AlerteGroupeDTO::getPriorite)
                .thenComparing(AlerteGroupeDTO::getNomGroupe, Comparator.nullsLast(String::compareTo)));

        return alertes;
    }

    @Override
    public List<AlerteGroupeDTO> detecterAnomaliesParType(String typeAnomalie) {
        return detecterToutesAnomalies().stream()
                .filter(a -> a.getTypeAnomalie().equalsIgnoreCase(typeAnomalie))
                .collect(Collectors.toList());
    }

    @Override
    public List<AlerteGroupeDTO> detecterAnomaliesParSeverite(String severite) {
        return detecterToutesAnomalies().stream()
                .filter(a -> a.getSeverite().equalsIgnoreCase(severite))
                .collect(Collectors.toList());
    }
}
