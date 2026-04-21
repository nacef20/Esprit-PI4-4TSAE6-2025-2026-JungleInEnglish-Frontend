package tn.esprit.gestion_groups.Services;

import tn.esprit.gestion_groups.dto.ScissionGroupeRequest;
import tn.esprit.gestion_groups.dto.ScissionResultatDTO;

public interface IScissionGroupeService {

    ScissionResultatDTO validerScission(ScissionGroupeRequest request);

    ScissionResultatDTO scinderGroupes(ScissionGroupeRequest request);
}