package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.CategorieReglement;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.ReglementGroupe;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.ReglementGroupeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReglementGroupeServiceImplTest {

    @Mock
    private ReglementGroupeRepository reglementRepository;

    @Mock
    private GroupeRepository groupeRepository;

    @InjectMocks
    private ReglementGroupeServiceImpl reglementService;

    @Test
    void addReglementAndAssignToGroupe_shouldAttachGroup() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(7L);

        ReglementGroupe reglement = new ReglementGroupe();
        reglement.setTitre("Ponctualite");

        when(groupeRepository.findById(7L)).thenReturn(Optional.of(groupe));
        when(reglementRepository.save(reglement)).thenReturn(reglement);

        ReglementGroupe result = reglementService.addReglementAndAssignToGroupe(reglement, 7L);

        assertEquals(groupe, result.getGroupe());
        verify(reglementRepository).save(reglement);
    }

    @Test
    void desactiverReglement_shouldUpdateFlag() {
        ReglementGroupe reglement = new ReglementGroupe();
        reglement.setIdReglement(3L);
        reglement.setEstActif(true);

        when(reglementRepository.findById(3L)).thenReturn(Optional.of(reglement));
        when(reglementRepository.save(reglement)).thenReturn(reglement);

        ReglementGroupe result = reglementService.desactiverReglement(3L);

        assertEquals(false, result.getEstActif());
    }

    @Test
    void getAllReglementsSimple_shouldMapDTOs() {
        ReglementGroupe reglement = new ReglementGroupe();
        reglement.setIdReglement(1L);
        reglement.setCodeReglement("REG-1");
        reglement.setTitre("Ponctualite");
        reglement.setCategorie(CategorieReglement.COMPORTEMENT);
        reglement.setEstActif(true);

        when(reglementRepository.findAll()).thenReturn(List.of(reglement));

        assertEquals(1, reglementService.getAllReglementsSimple().size());
        assertEquals("REG-1", reglementService.getAllReglementsSimple().get(0).getCodeReglement());
    }

    @Test
    void getReglementsByGroupe_shouldDelegateToRepository() {
        when(reglementRepository.findByGroupeIdGroupe(10L)).thenReturn(List.of());

        assertEquals(0, reglementService.getReglementsByGroupe(10L).size());
    }
}