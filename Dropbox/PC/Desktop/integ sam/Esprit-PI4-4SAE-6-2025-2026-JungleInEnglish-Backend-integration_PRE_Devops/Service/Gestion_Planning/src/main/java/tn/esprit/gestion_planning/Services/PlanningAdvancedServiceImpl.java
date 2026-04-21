package tn.esprit.gestion_planning.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_planning.DTO.*;
import tn.esprit.gestion_planning.Entites.*;
import tn.esprit.gestion_planning.Repositories.*;

import java.time.*;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlanningAdvancedServiceImpl implements IPlanningAdvancedService {

    private final PlanningSessionRepository planningSessionRepository;
    private final SalleRepository salleRepository;
    private final CreneauRepository creneauRepository;

    private static final double SEUIL_HEURES_ENSEIGNANT_PAR_SEMAINE = 30.0;
    private static final double SEUIL_HEURES_GROUPE_PAR_SEMAINE     = 35.0;
    private static final double SEUIL_TAUX_OCCUPATION_SALLE         = 80.0; // en %


    @Override
    public GenerationResult generateWeeklyPlanning(PlanningGeneratorRequest request) {

        List<PlanningSession> sessionsGenerees = new ArrayList<>();
        List<String> echecs = new ArrayList<>();

        List<CreneauHoraire> creneaux = creneauRepository.findAllById(request.getCreneauIds());
        List<Salle> salles = salleRepository.findAllById(request.getSalleIds());

        if (creneaux.isEmpty()) {
            echecs.add("Aucun créneau valide trouvé pour les IDs fournis.");
            return new GenerationResult(sessionsGenerees, echecs, 0, request.getAssignments().size());
        }
        if (salles.isEmpty()) {
            echecs.add("Aucune salle valide trouvée pour les IDs fournis.");
            return new GenerationResult(sessionsGenerees, echecs, 0, request.getAssignments().size());
        }

        List<LocalDate> joursOuvres = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            joursOuvres.add(request.getStartDate().plusDays(i));
        }

        Set<String> conflitsSalles      = new HashSet<>();
        Set<String> conflitsEnseignants = new HashSet<>();
        Set<String> conflitsGroupes     = new HashSet<>();

        LocalDate fin = request.getStartDate().plusDays(4);
        List<PlanningSession> existantes = planningSessionRepository
                .findByDatePlanningBetween(request.getStartDate(), fin);

        for (PlanningSession s : existantes) {
            String keyDate = s.getDatePlanning().toString();
            String keyCreneau = s.getCreneau().getIdCreneauHoraire().toString();

            if (s.getSalle() != null) {
                conflitsSalles.add(s.getSalle().getId() + "_" + keyCreneau + "_" + keyDate);
            }
            conflitsEnseignants.add(s.getEnseignantId() + "_" + keyCreneau + "_" + keyDate);
            conflitsGroupes.add(s.getGroupeId() + "_" + keyCreneau + "_" + keyDate);
        }


        for (AssignmentRequest assignment : request.getAssignments()) {
            boolean place = false;

            // Boucle sur les jours
            outerLoop:
            for (LocalDate jour : joursOuvres) {
                // Boucle sur les créneaux
                for (CreneauHoraire creneau : creneaux) {
                    String keyCreneauDate = creneau.getIdCreneauHoraire() + "_" + jour;

                    // Vérifier conflit enseignant
                    String keyEnseignant = assignment.getEnseignantId() + "_" + keyCreneauDate;
                    if (conflitsEnseignants.contains(keyEnseignant)) {
                        continue; // enseignant occupé, essayer prochain créneau
                    }

                    // Vérifier conflit groupe
                    String keyGroupe = assignment.getGroupeId() + "_" + keyCreneauDate;
                    if (conflitsGroupes.contains(keyGroupe)) {
                        continue; // groupe occupé, essayer prochain créneau
                    }

                    // Chercher une salle libre
                    Salle salleLibre = null;
                    for (Salle salle : salles) {
                        String keySalle = salle.getId() + "_" + keyCreneauDate;
                        if (!conflitsSalles.contains(keySalle)
                                && salle.getDisponibilite() == DisponibiliteSalle.DISPONIBLE) {
                            salleLibre = salle;
                            break;
                        }
                    }

                    if (salleLibre == null) {
                        continue;
                    }

                    PlanningSession session = new PlanningSession();
                    session.setGroupeId(assignment.getGroupeId());
                    session.setMatiereId(assignment.getMatiereId());
                    session.setEnseignantId(assignment.getEnseignantId());
                    session.setSalle(salleLibre);
                    session.setCreneau(creneau);
                    session.setDatePlanning(jour);
                    session.setStatut(StatutPlanning.PLANIFIE);
                    session.setCreatedAt(LocalDateTime.now());
                    session.setCreatedBy("AUTO_GENERATOR");
                    session.setObservations("Généré automatiquement");

                    PlanningSession saved = planningSessionRepository.save(session);
                    sessionsGenerees.add(saved);

                    conflitsSalles.add(salleLibre.getId() + "_" + keyCreneauDate);
                    conflitsEnseignants.add(assignment.getEnseignantId() + "_" + keyCreneauDate);
                    conflitsGroupes.add(assignment.getGroupeId() + "_" + keyCreneauDate);

                    place = true;
                    break outerLoop;
                }
            }

            if (!place) {
                echecs.add(String.format(
                        "Impossible de planifier : groupeId=%d | matiereId=%d | enseignantId=%d — aucun slot disponible sur la semaine.",
                        assignment.getGroupeId(),
                        assignment.getMatiereId(),
                        assignment.getEnseignantId()
                ));
            }
        }

        return new GenerationResult(
                sessionsGenerees,
                echecs,
                sessionsGenerees.size(),
                echecs.size()
        );
    }



    @Override
    public WeeklyLoadReport analyzeWeeklyLoad(LocalDate anyDayInWeek) {

        LocalDate debutSemaine = anyDayInWeek.with(
                WeekFields.ISO.dayOfWeek(), 1); // Lundi
        LocalDate finSemaine   = debutSemaine.plusDays(4); // Vendredi

        int weekNumber = anyDayInWeek.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year       = anyDayInWeek.get(IsoFields.WEEK_BASED_YEAR);

        List<PlanningSession> sessions =
                planningSessionRepository.findByDatePlanningBetween(debutSemaine, finSemaine);

        Map<Long, Double> heuresParEnseignant = new HashMap<>();
        Map<Long, Double> heuresParGroupe     = new HashMap<>();
        Map<Long, Double> heuresParSalle      = new HashMap<>();
        long totalCreneauxDisponibles = creneauRepository.count() * 5;

        for (PlanningSession session : sessions) {
            double dureeHeures = calculerDureeHeures(session);

            // ✅ Vérifier null avant d'insérer dans la map
            if (session.getEnseignantId() != null) {
                heuresParEnseignant.merge(session.getEnseignantId(), dureeHeures, Double::sum);
            }

            if (session.getGroupeId() != null) {
                heuresParGroupe.merge(session.getGroupeId(), dureeHeures, Double::sum);
            }

            if (session.getSalle() != null && session.getSalle().getId() != null) {
                heuresParSalle.merge(session.getSalle().getId(), dureeHeures, Double::sum);
            }
        }

        double dureeTheoriqueParJour = calculerDureeTotaleCreneaux();
        double dureeTheoriqueSemaine = dureeTheoriqueParJour * 5;

        Map<Long, Double> tauxOccupation = new HashMap<>();
        if (dureeTheoriqueSemaine > 0) {
            for (Map.Entry<Long, Double> entry : heuresParSalle.entrySet()) {
                double taux = (entry.getValue() / dureeTheoriqueSemaine) * 100.0;
                tauxOccupation.put(entry.getKey(), Math.round(taux * 100.0) / 100.0);
            }
        }

        List<String> alertes = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : heuresParEnseignant.entrySet()) {
            if (entry.getValue() > SEUIL_HEURES_ENSEIGNANT_PAR_SEMAINE) {
                alertes.add(String.format(
                        "⚠ SURCHARGE ENSEIGNANT [id=%d] : %.1f h cette semaine (seuil : %.0f h)",
                        entry.getKey(), entry.getValue(), SEUIL_HEURES_ENSEIGNANT_PAR_SEMAINE
                ));
            }
        }

        for (Map.Entry<Long, Double> entry : heuresParGroupe.entrySet()) {
            if (entry.getValue() > SEUIL_HEURES_GROUPE_PAR_SEMAINE) {
                alertes.add(String.format(
                        "⚠ SURCHARGE GROUPE [id=%d] : %.1f h cette semaine (seuil : %.0f h)",
                        entry.getKey(), entry.getValue(), SEUIL_HEURES_GROUPE_PAR_SEMAINE
                ));
            }
        }

        for (Map.Entry<Long, Double> entry : tauxOccupation.entrySet()) {
            if (entry.getValue() > SEUIL_TAUX_OCCUPATION_SALLE) {
                alertes.add(String.format(
                        "⚠ SALLE SATURÉE [id=%d] : taux d'occupation = %.1f%% (seuil : %.0f%%)",
                        entry.getKey(), entry.getValue(), SEUIL_TAUX_OCCUPATION_SALLE
                ));
            }
        }

        if (alertes.isEmpty()) {
            alertes.add("Aucune surcharge détectée pour la semaine " + weekNumber + "/" + year);
        }

        return new WeeklyLoadReport(
                weekNumber,
                year,
                heuresParEnseignant,
                heuresParGroupe,
                tauxOccupation,
                alertes
        );
    }



    private double calculerDureeHeures(PlanningSession session) {
        if (session.getCreneau() == null
                || session.getCreneau().getHeureDebut() == null
                || session.getCreneau().getHeureFin() == null) {
            return 0.0;
        }
        long minutes = Duration.between(
                session.getCreneau().getHeureDebut(),
                session.getCreneau().getHeureFin()
        ).toMinutes();
        return minutes / 60.0;
    }


    private double calculerDureeTotaleCreneaux() {
        List<CreneauHoraire> tousCreneaux = creneauRepository.findAll();
        double total = 0.0;
        for (CreneauHoraire c : tousCreneaux) {
            if (c.getHeureDebut() != null && c.getHeureFin() != null) {
                long minutes = Duration.between(c.getHeureDebut(), c.getHeureFin()).toMinutes();
                total += minutes / 60.0;
            }
        }
        return total;
    }

    // Dans PlanningAdvancedServiceImpl.java — ajouter cette méthode

    @Override
    public SemesterReplicationResult replicatePlanningSemester(SemesterReplicationRequest request) {

        // ============================================================
        // ÉTAPE 1 : Charger les sessions de la semaine de référence
        // ============================================================
        LocalDate debutReference = request.getSemaineReference(); // Lundi
        LocalDate finReference   = debutReference.plusDays(4);   // Vendredi

        List<PlanningSession> sessionsReference =
                planningSessionRepository.findByDatePlanningBetween(debutReference, finReference);

        if (sessionsReference.isEmpty()) {
            throw new RuntimeException(
                    "Aucune session trouvée dans la semaine de référence : " + debutReference
            );
        }

        // ============================================================
        // ÉTAPE 2 : Construire la liste de tous les lundis du semestre
        // ============================================================
        List<LocalDate> tousLesLundis = new ArrayList<>();
        LocalDate cursor = request.getDebutSemestre()
                .with(WeekFields.ISO.dayOfWeek(), 1); // Aller au lundi

        while (!cursor.isAfter(request.getFinSemestre())) {
            // Exclure la semaine de référence elle-même
            if (!cursor.isEqual(debutReference)) {
                tousLesLundis.add(cursor);
            }
            cursor = cursor.plusWeeks(1);
        }

        // ============================================================
        // ÉTAPE 3 : Construire les sets de jours fériés et semaines examen
        // ============================================================
        Set<LocalDate> joursFeriesSet = new HashSet<>(
                request.getJoursFeries() != null ? request.getJoursFeries() : new ArrayList<>()
        );
        Set<LocalDate> semainesExamensSet = new HashSet<>(
                request.getSemainesExamens() != null ? request.getSemainesExamens() : new ArrayList<>()
        );

        // ============================================================
        // ÉTAPE 4 : Variables de résultat
        // ============================================================
        Map<LocalDate, List<PlanningSession>> sessionsParSemaine      = new HashMap<>();
        Map<LocalDate, List<String>>          raisonsIgnoreesParSemaine = new HashMap<>();
        List<LocalDate> semainesBloques = new ArrayList<>();

        int totalSessionsCrees   = 0;
        int totalSessionsIgnorees = 0;
        int totalSemainesBloques = 0;

        // ============================================================
        // ÉTAPE 5 : Boucle principale sur chaque semaine du semestre
        // ============================================================
        for (LocalDate lundi : tousLesLundis) {

            // --- Vérifier si c'est une semaine d'examens ---
            if (semainesExamensSet.contains(lundi)) {
                semainesBloques.add(lundi);
                totalSemainesBloques++;
                continue; // Passer à la semaine suivante
            }

            List<PlanningSession> sessionsCreesCetteSemaine   = new ArrayList<>();
            List<String>          raisonsIgnoresCetteSemaine  = new ArrayList<>();

            // --- Boucle sur chaque session de référence ---
            for (PlanningSession reference : sessionsReference) {

                // Calculer le décalage en jours par rapport au lundi de référence
                long decalageJours = reference.getDatePlanning().toEpochDay()
                        - debutReference.toEpochDay();

                // Calculer la date cible dans la nouvelle semaine
                LocalDate dateCible = lundi.plusDays(decalageJours);

                // --- Vérifier si ce jour est un jour férié ---
                if (joursFeriesSet.contains(dateCible)) {
                    raisonsIgnoresCetteSemaine.add(String.format(
                            "Session ignorée [Groupe %d | Matière %d | Enseignant %d] " +
                                    "le %s → jour férié",
                            reference.getGroupeId(),
                            reference.getMatiereId(),
                            reference.getEnseignantId(),
                            dateCible
                    ));
                    totalSessionsIgnorees++;
                    continue;
                }

                // --- Vérifier conflit salle ---
                if (reference.getSalle() != null) {
                    List<PlanningSession> conflitSalle =
                            planningSessionRepository.findConflitSalle(
                                    reference.getSalle().getId(),
                                    reference.getCreneau().getIdCreneauHoraire(),
                                    dateCible
                            );
                    if (!conflitSalle.isEmpty()) {
                        raisonsIgnoresCetteSemaine.add(String.format(
                                "Session ignorée [Groupe %d | Matière %d | Enseignant %d] " +
                                        "le %s → conflit salle %s",
                                reference.getGroupeId(),
                                reference.getMatiereId(),
                                reference.getEnseignantId(),
                                dateCible,
                                reference.getSalle().getNom_salle()
                        ));
                        totalSessionsIgnorees++;
                        continue;
                    }
                }

                // --- Vérifier conflit enseignant ---
                List<PlanningSession> conflitEnseignant =
                        planningSessionRepository.findConflitEnseignant(
                                reference.getEnseignantId(),
                                reference.getCreneau().getIdCreneauHoraire(),
                                dateCible
                        );
                if (!conflitEnseignant.isEmpty()) {
                    raisonsIgnoresCetteSemaine.add(String.format(
                            "Session ignorée [Groupe %d | Matière %d | Enseignant %d] " +
                                    "le %s → enseignant déjà occupé",
                            reference.getGroupeId(),
                            reference.getMatiereId(),
                            reference.getEnseignantId(),
                            dateCible
                    ));
                    totalSessionsIgnorees++;
                    continue;
                }

                // --- Vérifier conflit groupe ---
                List<PlanningSession> conflitGroupe =
                        planningSessionRepository.findConflitGroupe(
                                reference.getGroupeId(),
                                reference.getCreneau().getIdCreneauHoraire(),
                                dateCible
                        );
                if (!conflitGroupe.isEmpty()) {
                    raisonsIgnoresCetteSemaine.add(String.format(
                            "Session ignorée [Groupe %d | Matière %d | Enseignant %d] " +
                                    "le %s → groupe déjà occupé",
                            reference.getGroupeId(),
                            reference.getMatiereId(),
                            reference.getEnseignantId(),
                            dateCible
                    ));
                    totalSessionsIgnorees++;
                    continue;
                }

                // ✅ Aucun conflit — créer la session répliquée
                PlanningSession nouvelleSession = new PlanningSession();
                nouvelleSession.setGroupeId(reference.getGroupeId());
                nouvelleSession.setMatiereId(reference.getMatiereId());
                nouvelleSession.setEnseignantId(reference.getEnseignantId());
                nouvelleSession.setSalle(reference.getSalle());
                nouvelleSession.setCreneau(reference.getCreneau());
                nouvelleSession.setDatePlanning(dateCible);
                nouvelleSession.setStatut(StatutPlanning.PLANIFIE);
                nouvelleSession.setObservations("Répliqué depuis semaine de référence : " + debutReference);
                nouvelleSession.setCreatedAt(LocalDateTime.now());
                nouvelleSession.setCreatedBy("SEMESTER_REPLICATOR");

                PlanningSession saved = planningSessionRepository.save(nouvelleSession);
                sessionsCreesCetteSemaine.add(saved);
                totalSessionsCrees++;
            }

            // Stocker les résultats de cette semaine
            if (!sessionsCreesCetteSemaine.isEmpty()) {
                sessionsParSemaine.put(lundi, sessionsCreesCetteSemaine);
            }
            if (!raisonsIgnoresCetteSemaine.isEmpty()) {
                raisonsIgnoreesParSemaine.put(lundi, raisonsIgnoresCetteSemaine);
            }
        }

        // ============================================================
        // ÉTAPE 6 : Retourner le résultat complet
        // ============================================================
        return new SemesterReplicationResult(
                tousLesLundis.size(),
                totalSemainesBloques,
                totalSessionsCrees,
                totalSessionsIgnorees,
                sessionsParSemaine,
                raisonsIgnoreesParSemaine,
                semainesBloques
        );
    }
}