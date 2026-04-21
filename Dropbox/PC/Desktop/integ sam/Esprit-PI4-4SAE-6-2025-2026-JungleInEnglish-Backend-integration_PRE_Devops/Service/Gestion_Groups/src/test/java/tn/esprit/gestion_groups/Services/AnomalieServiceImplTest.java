package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.ObjectifPedagogique;
import tn.esprit.gestion_groups.Entites.StatutObjectif;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.ObjectifPedagogiqueRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnomalieServiceImplTest {

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private ObjectifPedagogiqueRepository objectifRepository;

    @InjectMocks
    private AnomalieServiceImpl anomalieService;

    @Test
    void detecterToutesAnomalies_shouldAggregateAndSortIssues() {
        Groupe sansFormateur = new Groupe();
        sansFormateur.setIdGroupe(1L);
        sansFormateur.setNom("Alpha");
        sansFormateur.setCodeGroupe("G1");
        sansFormateur.setCapaciteActuelle(10);
        sansFormateur.setCapaciteMax(20);

        Groupe sansObjectif = new Groupe();
        sansObjectif.setIdGroupe(2L);
        sansObjectif.setNom("Beta");
        sansObjectif.setCodeGroupe("G2");
        sansObjectif.setCapaciteActuelle(5);
        sansObjectif.setCapaciteMax(10);

        ObjectifPedagogique objectif = new ObjectifPedagogique();
        objectif.setIdObjectif(4L);
        objectif.setTitre("Lire");
        objectif.setCodeObjectif("OBJ-1");
        objectif.setDateFinPrevue(LocalDate.now().minusDays(10));
        objectif.setStatut(StatutObjectif.EN_COURS);
        objectif.setGroupe(sansObjectif);

        when(groupeRepository.findGroupesSansFormateur(any())).thenReturn(List.of(sansFormateur));
        when(groupeRepository.findGroupesCapaciteIncoherente(any())).thenReturn(List.of());
        when(groupeRepository.findGroupesSansObjectif(any())).thenReturn(List.of(sansObjectif));
        when(objectifRepository.findObjectifsEnRetard(StatutObjectif.EN_COURS, LocalDate.now()))
                .thenReturn(List.of(objectif));

        assertEquals(3, anomalieService.detecterToutesAnomalies().size());
        assertEquals("CRITIQUE", anomalieService.detecterToutesAnomalies().get(0).getSeverite());
        assertTrue(anomalieService.detecterAnomaliesParSeverite("info").size() >= 1);
    }
}