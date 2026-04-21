package tn.esprit.gestion_planning.Services;

import tn.esprit.gestion_planning.Entites.Salle;

import java.util.List;

public interface ISalleService {

    Salle addSalle(Salle salle);

    Salle updateSalle(Salle salle);

    List<Salle> getAllSalles();

    Salle getSalleById(Long idSalle);

    void deleteSalle(Long idSalle);
}
