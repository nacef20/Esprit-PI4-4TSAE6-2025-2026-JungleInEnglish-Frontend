package tn.esprit.gestion_planning.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_planning.Entites.Salle;
import tn.esprit.gestion_planning.Services.ISalleService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/salles")
public class SalleController {

    private final ISalleService salleService;

    @GetMapping
    public List<Salle> getAllSalles() {
        return salleService.getAllSalles();
    }

    @GetMapping("/{idSalle}")
    public Salle getSalleById(@PathVariable Long idSalle) {
        return salleService.getSalleById(idSalle);
    }

    @PostMapping
    public Salle addSalle(@RequestBody Salle salle) {
        return salleService.addSalle(salle);
    }

    @PutMapping("/{idSalle}")
    public Salle updateSalle(@PathVariable Long idSalle, @RequestBody Salle salle) {
        salle.setId(idSalle);
        return salleService.updateSalle(salle);
    }

    @DeleteMapping("/{idSalle}")
    public void deleteSalle(@PathVariable Long idSalle) {
        salleService.deleteSalle(idSalle);
    }
}