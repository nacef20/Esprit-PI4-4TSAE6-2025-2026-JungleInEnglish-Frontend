package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.CompetenceCible;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.ObjectifPedagogique;
import tn.esprit.gestion_groups.Entites.StatutObjectif;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.ObjectifPedagogiqueRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObjectifPedagogiqueServiceImplTest {

    @Mock
    private ObjectifPedagogiqueRepository objectifRepository;

    @Mock
    private GroupeRepository groupeRepository;

    @InjectMocks
    private ObjectifPedagogiqueServiceImpl objectifService;

    @Test
    void addObjectifAndAssignToGroupe_shouldAttachGroup() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(9L);

        ObjectifPedagogique objectif = new ObjectifPedagogique();
        objectif.setTitre("Lire");

        when(groupeRepository.findById(9L)).thenReturn(Optional.of(groupe));
        when(objectifRepository.save(objectif)).thenReturn(objectif);

        ObjectifPedagogique result = objectifService.addObjectifAndAssignToGroupe(objectif, 9L);

        assertEquals(groupe, result.getGroupe());
        verify(objectifRepository).save(objectif);
    }

    @Test
    void marquerObjectifAtteint_shouldSwitchStatus() {
        ObjectifPedagogique objectif = new ObjectifPedagogique();
        objectif.setIdObjectif(4L);
        objectif.setStatut(StatutObjectif.EN_COURS);

        when(objectifRepository.findById(4L)).thenReturn(Optional.of(objectif));
        when(objectifRepository.save(objectif)).thenReturn(objectif);

        ObjectifPedagogique result = objectifService.marquerObjectifAtteint(4L);

        assertEquals(StatutObjectif.ATTEINT, result.getStatut());
    }

    @Test
    void getAllObjectifsSimple_shouldMapDTOs() {
        ObjectifPedagogique objectif = new ObjectifPedagogique();
        objectif.setIdObjectif(1L);
        objectif.setCodeObjectif("OBJ-1");
        objectif.setTitre("Comprendre");
        objectif.setCompetenceCible(CompetenceCible.READING);
        objectif.setStatut(StatutObjectif.EN_COURS);

        when(objectifRepository.findAll()).thenReturn(List.of(objectif));

        assertEquals(1, objectifService.getAllObjectifsSimple().size());
        assertEquals("OBJ-1", objectifService.getAllObjectifsSimple().get(0).getCodeObjectif());
    }
}
