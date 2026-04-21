package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.*;
import tn.esprit.gestion_groups.dto.FusionGroupeRequest;
import tn.esprit.gestion_groups.dto.FusionResultatDTO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FusionGroupeServiceImplTest {

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
    private FusionGroupeServiceImpl fusionService;

    @Test
    void validerFusion_shouldReportPossibleWhenTargetHasRoom() {
        Groupe source = new Groupe();
        source.setIdGroupe(1L);
        source.setCodeGroupe("SRC");
        source.setStatut(StatutGroupe.OUVERT);

        Groupe cible = new Groupe();
        cible.setIdGroupe(2L);
        cible.setCodeGroupe("DST");
        cible.setStatut(StatutGroupe.OUVERT);
        cible.setCapaciteActuelle(2);
        cible.setCapaciteMax(10);

        GroupeEtudiant etudiant = new GroupeEtudiant();
        etudiant.setEtudiantId(11L);

        FusionGroupeRequest request = new FusionGroupeRequest();
        request.setGroupeSourceId(1L);
        request.setGroupeCibleId(2L);
        request.setTransfererEtudiants(true);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(source));
        when(groupeRepository.findById(2L)).thenReturn(Optional.of(cible));
        when(groupeEtudiantRepository.findEtudiantsActifsByGroupe(1L)).thenReturn(List.of(etudiant));

        FusionResultatDTO result = fusionService.validerFusion(request);

        assertEquals("POSSIBLE", result.getStatut());
        assertEquals(1, result.getNombreEtudiantsTransferes());
    }

    @Test
    void fusionnerGroupes_shouldArchiveSourceWhenNoContentTransferred() {
        Groupe source = new Groupe();
        source.setIdGroupe(1L);
        source.setCodeGroupe("SRC");
        source.setNom("Source");
        source.setStatut(StatutGroupe.OUVERT);
        source.setCapaciteActuelle(5);

        Groupe cible = new Groupe();
        cible.setIdGroupe(2L);
        cible.setCodeGroupe("DST");
        cible.setNom("Cible");
        cible.setStatut(StatutGroupe.OUVERT);
        cible.setCapaciteActuelle(2);
        cible.setCapaciteMax(10);

        FusionGroupeRequest request = new FusionGroupeRequest();
        request.setGroupeSourceId(1L);
        request.setGroupeCibleId(2L);
        request.setTransfererEtudiants(false);
        request.setTransfererFormateurs(false);
        request.setTransfererObjectifs(false);
        request.setTransfererReglements(false);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(source));
        when(groupeRepository.findById(2L)).thenReturn(Optional.of(cible));
        when(groupeHealthScoreService.calculerScoreTotal(source)).thenReturn(70);
        when(groupeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FusionResultatDTO result = fusionService.fusionnerGroupes(request);

        assertEquals("SUCCES", result.getStatut());
        assertEquals(StatutGroupe.ANNULE, source.getStatut());
        assertEquals(0, source.getCapaciteActuelle());
    }
}