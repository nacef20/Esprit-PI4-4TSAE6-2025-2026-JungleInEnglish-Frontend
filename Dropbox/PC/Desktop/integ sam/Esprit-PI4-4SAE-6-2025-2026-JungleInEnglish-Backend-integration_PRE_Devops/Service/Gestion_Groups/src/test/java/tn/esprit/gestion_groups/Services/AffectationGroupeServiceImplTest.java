package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AffectationGroupeServiceImplTest {

    @Mock
    private GroupeEtudiantRepository groupeEtudiantRepository;

    @Mock
    private GroupeFormateurRepository groupeFormateurRepository;

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private HistoriqueGroupeRepository historiqueRepository;

    @Mock
    private GroupeHealthScoreService groupeHealthScoreService;

    @InjectMocks
    private AffectationGroupeServiceImpl affectationService;

    @Test
    void affecterEtudiantAGroupe_shouldCreateActiveAssignment() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setCodeGroupe("G1");
        groupe.setStatut(StatutGroupe.OUVERT);
        groupe.setCapaciteActuelle(0);
        groupe.setCapaciteMax(10);

        when(groupeEtudiantRepository.isEtudiantDejaAffecte(7L)).thenReturn(false);
        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(groupeEtudiantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GroupeEtudiant result = affectationService.affecterEtudiantAGroupe(7L, 1L, 70, "OK", 3L);

        assertEquals(7L, result.getEtudiantId());
        assertEquals(StatutAffectation.ACTIF, result.getStatut());
        assertEquals(1, groupe.getCapaciteActuelle());
    }

    @Test
    void suspendreEtudiant_shouldKeepCapacityReserved() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setCapaciteActuelle(1);
        GroupeEtudiant affectation = new GroupeEtudiant();
        affectation.setEtudiantId(7L);
        affectation.setGroupe(groupe);
        affectation.setStatut(StatutAffectation.ACTIF);

        when(groupeEtudiantRepository.findGroupeActuelByEtudiant(7L)).thenReturn(Optional.of(affectation));
        when(groupeEtudiantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GroupeEtudiant result = affectationService.suspendreEtudiant(7L, "pause");

        assertEquals(StatutAffectation.SUSPENDU, result.getStatut());
        assertEquals(1, groupe.getCapaciteActuelle());
    }

    @Test
    void reactiverEtudiant_shouldRestoreSuspendedAssignment() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        GroupeEtudiant affectation = new GroupeEtudiant();
        affectation.setEtudiantId(7L);
        affectation.setGroupe(groupe);
        affectation.setStatut(StatutAffectation.SUSPENDU);

        when(groupeEtudiantRepository.findByStatut(StatutAffectation.SUSPENDU)).thenReturn(List.of(affectation));
        when(groupeEtudiantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(StatutAffectation.ACTIF, affectationService.reactiverEtudiant(7L).getStatut());
    }
}