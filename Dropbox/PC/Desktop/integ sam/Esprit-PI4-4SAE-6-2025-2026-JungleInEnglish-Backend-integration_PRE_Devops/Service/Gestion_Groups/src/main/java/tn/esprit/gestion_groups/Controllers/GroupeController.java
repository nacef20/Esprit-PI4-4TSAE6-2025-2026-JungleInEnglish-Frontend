package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.StatutGroupe;
import tn.esprit.gestion_groups.Entites.TypeGroupe;
import tn.esprit.gestion_groups.dto.GroupeEnrichiDTO;
import tn.esprit.gestion_groups.dto.GroupeSimpleDTO;
import tn.esprit.gestion_groups.Services.IGroupeService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/groupes")
public class GroupeController {

    private final IGroupeService groupeService;

    @PostMapping("/add")
    public Groupe addGroupe(@RequestBody Groupe groupe) {
        return groupeService.addGroupe(groupe);
    }

    @PutMapping("/update")
    public Groupe updateGroupe(@RequestBody Groupe groupe) {
        return groupeService.updateGroupe(groupe);
    }

    @GetMapping("/all")
    public List<GroupeSimpleDTO> getAllGroupes() {
        return groupeService.getAllGroupesSimple();
    }

    @GetMapping("/{idGroupe}")
    public Groupe getGroupeById(@PathVariable Long idGroupe) {
        return groupeService.getGroupeWithDetails(idGroupe);
    }

    @GetMapping("/{idGroupe}/enrichi")
    public ResponseEntity<GroupeEnrichiDTO> getGroupeEnrichi(@PathVariable Long idGroupe) {
        GroupeEnrichiDTO groupe = groupeService.getGroupeEnrichi(idGroupe);
        return groupe != null ? ResponseEntity.ok(groupe) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{idGroupe}")
    public void deleteGroupe(@PathVariable Long idGroupe) {
        groupeService.deleteGroupe(idGroupe);
    }

    @GetMapping("/code/{codeGroupe}")
    public Groupe getGroupeByCode(@PathVariable String codeGroupe) {
        return groupeService.getGroupeByCode(codeGroupe);
    }

    @GetMapping("/statut/{statut}")
    public List<Groupe> getGroupesByStatut(@PathVariable StatutGroupe statut) {
        return groupeService.getGroupesByStatut(statut);
    }

    @GetMapping("/type/{type}")
    public List<Groupe> getGroupesByType(@PathVariable TypeGroupe type) {
        return groupeService.getGroupesByType(type);
    }

    @GetMapping("/niveau/{niveauId}")
    public List<Groupe> getGroupesByNiveau(@PathVariable Long niveauId) {
        return groupeService.getGroupesByNiveau(niveauId);
    }

    @GetMapping("/disponibles")
    public List<Groupe> getGroupesDisponibles() {
        return groupeService.getGroupesDisponibles();
    }

    @GetMapping("/search/{nom}")
    public List<Groupe> searchGroupesByNom(@PathVariable String nom) {
        return groupeService.searchGroupesByNom(nom);
    }

    @PutMapping("/update-statut/{idGroupe}/{nouveauStatut}")
    public Groupe updateStatutGroupe(
            @PathVariable Long idGroupe,
            @PathVariable StatutGroupe nouveauStatut) {
        return groupeService.updateStatutGroupe(idGroupe, nouveauStatut);
    }

    @PutMapping("/incrementer-capacite/{idGroupe}")
    public Groupe incrementerCapacite(@PathVariable Long idGroupe) {
        return groupeService.incrementerCapacite(idGroupe);
    }

    @PutMapping("/decrementer-capacite/{idGroupe}")
    public Groupe decrementerCapacite(@PathVariable Long idGroupe) {
        return groupeService.decrementerCapacite(idGroupe);
    }
}