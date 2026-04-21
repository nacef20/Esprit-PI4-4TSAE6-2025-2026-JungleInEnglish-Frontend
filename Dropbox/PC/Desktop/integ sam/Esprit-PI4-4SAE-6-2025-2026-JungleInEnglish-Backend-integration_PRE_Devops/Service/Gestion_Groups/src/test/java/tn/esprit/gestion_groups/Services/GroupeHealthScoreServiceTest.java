package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.ObjectifPedagogique;
import tn.esprit.gestion_groups.Entites.ReglementGroupe;
import tn.esprit.gestion_groups.Entites.StatutObjectif;
import tn.esprit.gestion_groups.Repositories.HistoriqueGroupeRepository;
import tn.esprit.gestion_groups.Repositories.ObjectifPedagogiqueRepository;
import tn.esprit.gestion_groups.Repositories.ReglementGroupeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupeHealthScoreServiceTest {

    @Mock
    private ObjectifPedagogiqueRepository objectifRepository;

    @Mock
    private ReglementGroupeRepository reglementRepository;

    @Mock
    private HistoriqueGroupeRepository historiqueRepository;

    @InjectMocks
    private GroupeHealthScoreService healthScoreService;

    @Test
    void calculerScoreTotal_shouldCombineAllDimensions() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setCapaciteActuelle(10);
        groupe.setCapaciteMax(20);

        ObjectifPedagogique objectif = new ObjectifPedagogique();
        objectif.setStatut(StatutObjectif.ATTEINT);
        ReglementGroupe reglement = new ReglementGroupe();
        reglement.setEstObligatoire(true);
        reglement.setEstActif(true);

        when(objectifRepository.findByGroupeIdGroupeAndStatut(1L, StatutObjectif.ATTEINT)).thenReturn(List.of(objectif));
        when(objectifRepository.findByGroupeIdGroupe(1L)).thenReturn(List.of(objectif));
        when(reglementRepository.findByGroupeIdGroupeAndEstActif(1L, true)).thenReturn(List.of(reglement));
        when(reglementRepository.findByGroupeIdGroupe(1L)).thenReturn(List.of(reglement));
        when(historiqueRepository.countChangementsRecentsByGroupe(any(Long.class), any())).thenReturn(1L);

        Integer score = healthScoreService.calculerScoreTotal(groupe);

        assertEquals(83, score);
    }

    @Test
    void calculerScoreTotal_shouldReturnNullForNullGroup() {
        assertEquals(null, healthScoreService.calculerScoreTotal(null));
    }
}
