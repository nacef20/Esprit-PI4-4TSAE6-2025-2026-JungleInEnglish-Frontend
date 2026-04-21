package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.Entites.CompetenceCible;
import tn.esprit.gestion_groups.Entites.ObjectifPedagogique;
import tn.esprit.gestion_groups.Entites.StatutObjectif;
import tn.esprit.gestion_groups.dto.ObjectifSimpleDTO;
import tn.esprit.gestion_groups.Services.IObjectifPedagogiqueService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/objectifs")
public class ObjectifPedagogiqueController {

    private final IObjectifPedagogiqueService objectifService;

    @PostMapping("/add")
    public ObjectifPedagogique addObjectif(@RequestBody ObjectifPedagogique objectif) {
        return objectifService.addObjectif(objectif);
    }

    @PutMapping("/update")
    public ObjectifPedagogique updateObjectif(@RequestBody ObjectifPedagogique objectif) {
        return objectifService.updateObjectif(objectif);
    }

    @GetMapping("/all")
    public List<ObjectifSimpleDTO> getAllObjectifs() {
        return objectifService.getAllObjectifsSimple();
    }

    @GetMapping("/{idObjectif}")
    public ObjectifPedagogique getObjectifById(@PathVariable Long idObjectif) {
        return objectifService.getObjectifById(idObjectif);
    }

    @DeleteMapping("/delete/{idObjectif}")
    public void deleteObjectif(@PathVariable Long idObjectif) {
        objectifService.deleteObjectif(idObjectif);
    }

    @PostMapping("/add-and-assign/{groupeId}")
    public ObjectifPedagogique addObjectifAndAssignToGroupe(
            @RequestBody ObjectifPedagogique objectif,
            @PathVariable Long groupeId) {
        return objectifService.addObjectifAndAssignToGroupe(objectif, groupeId);
    }

    @PutMapping("/affecter/{objectifId}/{groupeId}")
    public void assignObjectifToGroupe(
            @PathVariable Long objectifId,
            @PathVariable Long groupeId) {
        objectifService.assignObjectifToGroupe(objectifId, groupeId);
    }

    @PutMapping("/desaffecter/{objectifId}")
    public ObjectifPedagogique desaffecterObjectifFromGroupe(@PathVariable Long objectifId) {
        return objectifService.desaffecterObjectifFromGroupe(objectifId);
    }

    @GetMapping("/groupe/{groupeId}")
    public List<ObjectifPedagogique> getObjectifsByGroupe(@PathVariable Long groupeId) {
        return objectifService.getObjectifsByGroupe(groupeId);
    }

    @GetMapping("/statut/{statut}")
    public List<ObjectifPedagogique> getObjectifsByStatut(@PathVariable StatutObjectif statut) {
        return objectifService.getObjectifsByStatut(statut);
    }

    @GetMapping("/competence/{competence}")
    public List<ObjectifPedagogique> getObjectifsByCompetence(@PathVariable CompetenceCible competence) {
        return objectifService.getObjectifsByCompetence(competence);
    }

    @GetMapping("/groupe/{groupeId}/statut/{statut}")
    public List<ObjectifPedagogique> getObjectifsByGroupeAndStatut(
            @PathVariable Long groupeId,
            @PathVariable StatutObjectif statut) {
        return objectifService.getObjectifsByGroupeAndStatut(groupeId, statut);
    }

    @GetMapping("/groupe/{groupeId}/priorite")
    public List<ObjectifPedagogique> getObjectifsOrderedByPriorite(@PathVariable Long groupeId) {
        return objectifService.getObjectifsOrderedByPriorite(groupeId);
    }

    @PutMapping("/marquer-atteint/{idObjectif}")
    public ObjectifPedagogique marquerObjectifAtteint(@PathVariable Long idObjectif) {
        return objectifService.marquerObjectifAtteint(idObjectif);
    }

    @PutMapping("/update-statut/{idObjectif}/{nouveauStatut}")
    public ObjectifPedagogique updateStatutObjectif(
            @PathVariable Long idObjectif,
            @PathVariable StatutObjectif nouveauStatut) {
        return objectifService.updateStatutObjectif(idObjectif, nouveauStatut);
    }

    @PutMapping("/update-taux-reussite/{idObjectif}/{tauxReussite}")
    public ObjectifPedagogique updateTauxReussite(
            @PathVariable Long idObjectif,
            @PathVariable Integer tauxReussite) {
        return objectifService.updateTauxReussite(idObjectif, tauxReussite);
    }
}