package tn.esprit.gestion_planning.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.gestion_planning.Entites.PlanningSession;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlanningSessionRepository extends JpaRepository<PlanningSession,Long> {

    List<PlanningSession> findByDatePlanningBetween(LocalDate start, LocalDate end);

    @Query("SELECT p FROM PlanningSession p WHERE p.salle.id = :salleId " +
            "AND p.creneau.idCreneauHoraire = :creneauId " +
            "AND p.datePlanning = :date")
    List<PlanningSession> findConflitSalle(@Param("salleId") Long salleId,
                                           @Param("creneauId") Long creneauId,
                                           @Param("date") LocalDate date);

    @Query("SELECT p FROM PlanningSession p WHERE p.enseignantId = :enseignantId " +
            "AND p.creneau.idCreneauHoraire = :creneauId " +
            "AND p.datePlanning = :date")
    List<PlanningSession> findConflitEnseignant(@Param("enseignantId") Long enseignantId,
                                                @Param("creneauId") Long creneauId,
                                                @Param("date") LocalDate date);

    @Query("SELECT p FROM PlanningSession p WHERE p.groupeId = :groupeId " +
            "AND p.creneau.idCreneauHoraire = :creneauId " +
            "AND p.datePlanning = :date")
    List<PlanningSession> findConflitGroupe(@Param("groupeId") Long groupeId,
                                            @Param("creneauId") Long creneauId,
                                            @Param("date") LocalDate date);


    // Vérifier si une session identique existe déjà
    @Query("SELECT p FROM PlanningSession p " +
            "WHERE p.enseignantId = :enseignantId " +
            "AND p.groupeId = :groupeId " +
            "AND p.matiereId = :matiereId " +
            "AND p.creneau.idCreneauHoraire = :creneauId " +
            "AND p.datePlanning = :date")
    List<PlanningSession> findSessionIdentique(
            @Param("enseignantId") Long enseignantId,
            @Param("groupeId") Long groupeId,
            @Param("matiereId") Long matiereId,
            @Param("creneauId") Long creneauId,
            @Param("date") LocalDate date);

}
