package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.GroupeEtudiant;
import tn.esprit.gestion_groups.Entites.HistoriqueGroupe;
import tn.esprit.gestion_groups.Entites.StatutAffectation;
import tn.esprit.gestion_groups.Repositories.GroupeEtudiantRepository;
import tn.esprit.gestion_groups.Repositories.HistoriqueGroupeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditTrailServiceImplTest {

    @Mock
    private GroupeEtudiantRepository groupeEtudiantRepository;

    @Mock
    private HistoriqueGroupeRepository historiqueRepository;

    @InjectMocks
    private AuditTrailServiceImpl auditTrailService;

    @Test
    void getParcoursEtudiant_shouldMergeAffectationsAndHistory() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setNom("Alpha");
        groupe.setCodeGroupe("G1");

        GroupeEtudiant affectation = new GroupeEtudiant();
        affectation.setEtudiantId(12L);
        affectation.setGroupe(groupe);
        affectation.setStatut(StatutAffectation.ACTIF);
        affectation.setDateAffectation(LocalDate.now().minusDays(3));
        affectation.setScoreEntree(55);

        HistoriqueGroupe historique = new HistoriqueGroupe();
        historique.setGroupe(groupe);
        historique.setGroupePrecedentId(99L);
        historique.setDateChangement(LocalDateTime.now().minusDays(1));
        historique.setScoreAvant(55);
        historique.setScoreApres(72);
        historique.setRaisonChangement(tn.esprit.gestion_groups.Entites.RaisonChangement.REEQUILIBRAGE);

        when(groupeEtudiantRepository.findParcoursCompletEtudiant(12L)).thenReturn(List.of(affectation));
        when(historiqueRepository.findByEtudiantIdOrderByDateChangementAsc(12L)).thenReturn(List.of(historique));

        assertEquals(1, auditTrailService.getParcoursEtudiant(12L).size());
        assertEquals(1L, auditTrailService.getParcoursEtudiant(12L).get(0).getIdGroupe());
    }

    @Test
    void getStatistiquesRetention_shouldMapRetentionRows() {
        when(groupeEtudiantRepository.getStatistiquesRetentionParGroupe(1L)).thenReturn(java.util.Collections.<Object[]>singletonList(
            new Object[]{1L, "Alpha", 10L, 2L, 1L, 7L}
        ));
        when(groupeEtudiantRepository.getDureeMoyenneSejourParGroupe()).thenReturn(java.util.Collections.<Object[]>singletonList(
            new Object[]{1L, 4.5}
        ));

        assertEquals(1, auditTrailService.getStatistiquesRetention(1L).size());
        assertEquals(70.0, auditTrailService.getStatistiquesRetention(1L).get(0).getTauxRetention());
    }
}