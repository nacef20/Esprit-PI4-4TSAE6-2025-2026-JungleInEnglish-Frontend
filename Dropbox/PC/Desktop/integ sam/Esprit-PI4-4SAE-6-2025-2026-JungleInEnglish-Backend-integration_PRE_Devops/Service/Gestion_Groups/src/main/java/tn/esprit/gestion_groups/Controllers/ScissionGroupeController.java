package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.Services.IScissionGroupeService;
import tn.esprit.gestion_groups.dto.ScissionGroupeRequest;
import tn.esprit.gestion_groups.dto.ScissionResultatDTO;

@RestController
@AllArgsConstructor
@RequestMapping("/scission")
public class ScissionGroupeController {

    private final IScissionGroupeService scissionService;

    @PostMapping("/valider")
    public ScissionResultatDTO validerScission(@RequestBody ScissionGroupeRequest request) {
        return scissionService.validerScission(request);
    }

    @PostMapping("/executer")
    public ScissionResultatDTO executerScission(@RequestBody ScissionGroupeRequest request) {
        return scissionService.scinderGroupes(request);
    }
}