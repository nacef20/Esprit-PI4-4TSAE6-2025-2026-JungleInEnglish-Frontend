package tn.esprit.gestion_planning.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_planning.Entites.CreneauHoraire;
import tn.esprit.gestion_planning.Services.ICreneauHoraireService;

import java.util.List;

@RestController
@AllArgsConstructor

@RequestMapping("/creneaux")
public class CreneauHoraireController {

    private final ICreneauHoraireService creneauHoraireService;

    @GetMapping
    public List<CreneauHoraire> getAllCreneauHoraires() {
        return creneauHoraireService.getAllCreneauHoraires();
    }

    @GetMapping("/{idCreneauHoraire}")
    public CreneauHoraire getCreneauHoraireById(@PathVariable Long idCreneauHoraire) {
        return creneauHoraireService.getCreneauHoraireById(idCreneauHoraire);
    }

    @PostMapping
    public CreneauHoraire addCreneauHoraire(@RequestBody CreneauHoraire creneauHoraire) {
        return creneauHoraireService.addCreneauHoraire(creneauHoraire);
    }

    @PutMapping("/{idCreneauHoraire}")
    public CreneauHoraire updateCreneauHoraire(@PathVariable Long idCreneauHoraire, @RequestBody CreneauHoraire creneauHoraire) {
        return creneauHoraireService.updateCreneauHoraire(creneauHoraire);
    }

    @DeleteMapping("/{idCreneauHoraire}")
    public void deleteCreneauHoraire(@PathVariable Long idCreneauHoraire) {
        creneauHoraireService.deleteCreneauHoraire(idCreneauHoraire);
    }
}