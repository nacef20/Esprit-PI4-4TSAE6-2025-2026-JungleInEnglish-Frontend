package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.*;
import tn.esprit.gestion_groups.dto.ScissionGroupeRequest;
import tn.esprit.gestion_groups.dto.ScissionResultatDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScissionGroupeServiceImplTest {

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private GroupeEtudiantRepository groupeEtudiantRepository;

    @Mock
    private GroupeFormateurRepository groupeFormateurRepository;

    @Mock
    private ObjectifPedagogiqueRepository objectifRepository;

    @Mock
    private ReglementGroupeRepository reglementRepository;

    @Mock
    private HistoriqueGroupeRepository historiqueRepository;

    @Mock
    private GroupeHealthScoreService groupeHealthScoreService;

    @InjectMocks
    private ScissionGroupeServiceImpl scissionService;

    @Test
    void validerScission_shouldReportPossible() {
        Groupe source = new Groupe();
        source.setIdGroupe(1L);
        source.setCodeGroupe("SRC");
        source.setStatut(StatutGroupe.OUVERT);
        source.setCapaciteActuelle(3);
        source.setCapaciteMax(10);

        Groupe cible = new Groupe();
        cible.setIdGroupe(2L);
        cible.setCodeGroupe("DST");
        cible.setStatut(StatutGroupe.OUVERT);
        cible.setCapaciteActuelle(1);
        cible.setCapaciteMax(10);

        GroupeEtudiant etudiant = new GroupeEtudiant();
        etudiant.setEtudiantId(11L);
        etudiant.setDateAffectation(LocalDate.now().minusDays(2));

        ScissionGroupeRequest request = new ScissionGroupeRequest();
        request.setGroupeSourceId(1L);
        request.setGroupeCibleId(2L);
        request.setNombreEtudiantsADeplacer(1);
        request.setTransfererEtudiants(true);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(source));
        when(groupeRepository.findById(2L)).thenReturn(Optional.of(cible));
        when(groupeEtudiantRepository.findEtudiantsActifsByGroupe(1L)).thenReturn(List.of(etudiant));

        ScissionResultatDTO result = scissionService.validerScission(request);

        assertEquals("POSSIBLE", result.getStatut());
        assertEquals(1, result.getNombreEtudiantsTransferes());
    }

    @Test
    void scinderGroupes_shouldMoveOneStudent() {
        Groupe source = new Groupe();
        source.setIdGroupe(1L);
        source.setCodeGroupe("SRC");
        source.setNom("Source");
        source.setStatut(StatutGroupe.OUVERT);
        source.setCapaciteActuelle(1);
        source.setCapaciteMax(10);

        Groupe cible = new Groupe();
        cible.setIdGroupe(2L);
        cible.setCodeGroupe("DST");
        cible.setNom("Cible");
        cible.setStatut(StatutGroupe.OUVERT);
        cible.setCapaciteActuelle(0);
        cible.setCapaciteMax(10);

        GroupeEtudiant etudiant = new GroupeEtudiant();
        etudiant.setEtudiantId(11L);
        etudiant.setGroupe(source);
        etudiant.setDateAffectation(LocalDate.now().minusDays(2));
        etudiant.setStatut(StatutAffectation.ACTIF);

        ScissionGroupeRequest request = new ScissionGroupeRequest();
        request.setGroupeSourceId(1L);
        request.setGroupeCibleId(2L);
        request.setNombreEtudiantsADeplacer(1);
        request.setTransfererEtudiants(true);
        request.setCommentaire("test");
        request.setDeclenchePar(99L);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(source));
        when(groupeRepository.findById(2L)).thenReturn(Optional.of(cible));
        when(groupeEtudiantRepository.findEtudiantsActifsByGroupe(1L)).thenReturn(List.of(etudiant));
        when(groupeEtudiantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(historiqueRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupeHealthScoreService.calculerScoreTotal(source)).thenReturn(60);
        when(groupeHealthScoreService.calculerScoreTotal(cible)).thenReturn(70);

        ScissionResultatDTO result = scissionService.scinderGroupes(request);

        assertEquals("SUCCES", result.getStatut());
        assertEquals(1, result.getNombreEtudiantsTransferes());
        assertEquals(0, source.getCapaciteActuelle());
        assertEquals(1, cible.getCapaciteActuelle());
    }
}