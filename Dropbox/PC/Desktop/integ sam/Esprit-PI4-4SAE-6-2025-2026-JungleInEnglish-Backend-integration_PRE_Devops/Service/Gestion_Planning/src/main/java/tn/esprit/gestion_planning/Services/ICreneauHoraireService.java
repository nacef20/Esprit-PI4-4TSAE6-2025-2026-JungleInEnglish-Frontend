package tn.esprit.gestion_planning.Services;

import tn.esprit.gestion_planning.Entites.CreneauHoraire;

import java.util.List;

public interface ICreneauHoraireService {

    CreneauHoraire addCreneauHoraire(CreneauHoraire creneauHoraire);

    CreneauHoraire updateCreneauHoraire(CreneauHoraire creneauHoraire);

    List<CreneauHoraire> getAllCreneauHoraires();

    CreneauHoraire getCreneauHoraireById(Long idCreneauHoraire);

    void deleteCreneauHoraire(Long idCreneauHoraire);
}
