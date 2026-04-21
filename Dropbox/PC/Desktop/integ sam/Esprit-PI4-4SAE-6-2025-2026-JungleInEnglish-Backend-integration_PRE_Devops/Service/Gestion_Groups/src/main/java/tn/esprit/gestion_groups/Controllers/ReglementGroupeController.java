package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.Entites.CategorieReglement;
import tn.esprit.gestion_groups.Entites.ReglementGroupe;
import tn.esprit.gestion_groups.dto.ReglementSimpleDTO;
import tn.esprit.gestion_groups.Services.IReglementGroupeService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/reglements")
public class ReglementGroupeController {

    private final IReglementGroupeService reglementService;

    @PostMapping("/add")
    public ReglementGroupe addReglement(@RequestBody ReglementGroupe reglement) {
        return reglementService.addReglement(reglement);
    }

    @PutMapping("/update")
    public ReglementGroupe updateReglement(@RequestBody ReglementGroupe reglement) {
        return reglementService.updateReglement(reglement);
    }

    @GetMapping("/all")
    public List<ReglementSimpleDTO> getAllReglements() {
        return reglementService.getAllReglementsSimple();
    }

    @GetMapping("/{idReglement}")
    public ReglementGroupe getReglementById(@PathVariable Long idReglement) {
        return reglementService.getReglementById(idReglement);
    }

    @DeleteMapping("/delete/{idReglement}")
    public void deleteReglement(@PathVariable Long idReglement) {
        reglementService.deleteReglement(idReglement);
    }

    @PostMapping("/add-and-assign/{groupeId}")
    public ReglementGroupe addReglementAndAssignToGroupe(
            @RequestBody ReglementGroupe reglement,
            @PathVariable Long groupeId) {
        return reglementService.addReglementAndAssignToGroupe(reglement, groupeId);
    }

    @PutMapping("/affecter/{reglementId}/{groupeId}")
    public void assignReglementToGroupe(
            @PathVariable Long reglementId,
            @PathVariable Long groupeId) {
        reglementService.assignReglementToGroupe(reglementId, groupeId);
    }

    @PutMapping("/desaffecter/{reglementId}")
    public ReglementGroupe desaffecterReglementFromGroupe(@PathVariable Long reglementId) {
        return reglementService.desaffecterReglementFromGroupe(reglementId);
    }

    @GetMapping("/groupe/{groupeId}")
    public List<ReglementGroupe> getReglementsByGroupe(@PathVariable Long groupeId) {
        return reglementService.getReglementsByGroupe(groupeId);
    }

    @GetMapping("/categorie/{categorie}")
    public List<ReglementGroupe> getReglementsByCategorie(@PathVariable CategorieReglement categorie) {
        return reglementService.getReglementsByCategorie(categorie);
    }

    @GetMapping("/actifs")
    public List<ReglementGroupe> getReglementsActifs() {
        return reglementService.getReglementsActifs();
    }

    @GetMapping("/actifs/groupe/{groupeId}")
    public List<ReglementGroupe> getReglementsActifsByGroupe(@PathVariable Long groupeId) {
        return reglementService.getReglementsActifsByGroupe(groupeId);
    }

    @PutMapping("/activer/{idReglement}")
    public ReglementGroupe activerReglement(@PathVariable Long idReglement) {
        return reglementService.activerReglement(idReglement);
    }

    @PutMapping("/desactiver/{idReglement}")
    public ReglementGroupe desactiverReglement(@PathVariable Long idReglement) {
        return reglementService.desactiverReglement(idReglement);
    }
}