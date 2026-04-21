package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.GroupeFormateur;
import tn.esprit.gestion_groups.Entites.StatutGroupe;
import tn.esprit.gestion_groups.Entites.TypeGroupe;
import tn.esprit.gestion_groups.Entites.RoleFormateur;
import tn.esprit.gestion_groups.Repositories.GroupeFormateurRepository;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.clients.UserFeignClient;
import tn.esprit.gestion_groups.dto.GroupeEnrichiDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupeServiceImplTest {

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private GroupeFormateurRepository groupeFormateurRepository;

    @Mock
    private UserFeignClient userFeignClient;

    @InjectMocks
    private GroupeServiceImpl groupeService;

    @Test
    void getAllGroupesSimple_shouldMapBasicFields() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setCodeGroupe("G1");
        groupe.setNom("Groupe 1");
        groupe.setNiveauId(2L);
        groupe.setCapaciteMin(10);
        groupe.setCapaciteMax(20);
        groupe.setCapaciteActuelle(12);
        groupe.setStatut(StatutGroupe.OUVERT);
        groupe.setType(TypeGroupe.ETUDIANT);

        when(groupeRepository.findAll()).thenReturn(List.of(groupe));

        assertEquals(1, groupeService.getAllGroupesSimple().size());
        assertEquals("G1", groupeService.getAllGroupesSimple().get(0).getCodeGroupe());
    }

    @Test
    void updateStatutGroupe_shouldPersistNewStatus() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setStatut(StatutGroupe.OUVERT);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(groupeRepository.save(groupe)).thenReturn(groupe);

        Groupe result = groupeService.updateStatutGroupe(1L, StatutGroupe.COMPLET);

        assertEquals(StatutGroupe.COMPLET, result.getStatut());
        verify(groupeRepository).save(groupe);
    }

    @Test
    void incrementerCapacite_shouldSwitchToCompletAtMax() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setCapaciteActuelle(9);
        groupe.setCapaciteMax(10);
        groupe.setStatut(StatutGroupe.OUVERT);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(groupeRepository.save(groupe)).thenReturn(groupe);

        Groupe result = groupeService.incrementerCapacite(1L);

        assertEquals(10, result.getCapaciteActuelle());
        assertEquals(StatutGroupe.COMPLET, result.getStatut());
    }

    @Test
    void decrementerCapacite_shouldReopenCompletGroup() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setCapaciteActuelle(10);
        groupe.setCapaciteMax(10);
        groupe.setStatut(StatutGroupe.COMPLET);

        when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        when(groupeRepository.save(groupe)).thenReturn(groupe);

        Groupe result = groupeService.decrementerCapacite(1L);

        assertEquals(9, result.getCapaciteActuelle());
        assertEquals(StatutGroupe.OUVERT, result.getStatut());
    }

    @Test
    void getGroupeById_shouldReturnNullWhenMissing() {
        when(groupeRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(groupeService.getGroupeById(99L));
    }

    @Test
    void getGroupeEnrichi_shouldMergeUserserviceData() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setCodeGroupe("G1");
        groupe.setNom("Groupe 1");
        groupe.setNiveauId(2L);
        groupe.setCapaciteMin(10);
        groupe.setCapaciteMax(20);
        groupe.setCapaciteActuelle(12);
        groupe.setDateDebut(LocalDate.of(2026, 4, 1));
        groupe.setDateFin(LocalDate.of(2026, 6, 30));
        groupe.setStatut(StatutGroupe.EN_COURS);
        groupe.setType(TypeGroupe.ETUDIANT);

        GroupeFormateur groupeFormateur = new GroupeFormateur();
        groupeFormateur.setFormateurId(42L);
        groupeFormateur.setRole(RoleFormateur.PRINCIPAL);
        groupeFormateur.setEstActif(true);
        groupeFormateur.setDateDebut(LocalDate.of(2026, 4, 1));
        groupeFormateur.setMatiere("Oral");
        groupeFormateur.setHeuresParSemaine(4);
        groupeFormateur.setCommentaire("Responsable principal");
        groupeFormateur.setGroupe(groupe);

        UserFeignClient.UtilisateurResumeDTO utilisateur = UserFeignClient.UtilisateurResumeDTO.builder()
                .id("42")
                .username("teacher42")
                .firstName("Amine")
                .lastName("Ben Ali")
                .email("amine@example.com")
                .build();

        when(groupeRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(groupe));
        when(groupeFormateurRepository.findFormateursActifsByGroupe(1L)).thenReturn(List.of(groupeFormateur));
        when(userFeignClient.getUserById("42")).thenReturn(utilisateur);

        GroupeEnrichiDTO result = groupeService.getGroupeEnrichi(1L);

        assertNotNull(result);
        assertEquals("G1", result.getCodeGroupe());
        assertEquals(1, result.getNombreFormateursActifs());
        assertEquals("teacher42", result.getFormateurs().get(0).getUsername());
        assertEquals("Amine", result.getFormateurs().get(0).getFirstName());
        assertEquals("Oral", result.getFormateurs().get(0).getMatiere());
    }
}