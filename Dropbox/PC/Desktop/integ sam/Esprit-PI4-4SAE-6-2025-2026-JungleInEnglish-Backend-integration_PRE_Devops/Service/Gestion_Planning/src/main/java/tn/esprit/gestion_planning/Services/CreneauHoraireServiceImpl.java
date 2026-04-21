package tn.esprit.gestion_planning.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_planning.Entites.CreneauHoraire;
import tn.esprit.gestion_planning.Repositories.CreneauRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CreneauHoraireServiceImpl implements ICreneauHoraireService {

    final CreneauRepository creneauRepository;

    @Override
    public CreneauHoraire addCreneauHoraire(CreneauHoraire creneauHoraire) {
        return creneauRepository.save(creneauHoraire);
    }

    @Override
    public CreneauHoraire updateCreneauHoraire(CreneauHoraire creneauHoraire) {
        return creneauRepository.save(creneauHoraire);
    }

    @Override
    public List<CreneauHoraire> getAllCreneauHoraires() {
        return creneauRepository.findAll();
    }

    @Override
    public CreneauHoraire getCreneauHoraireById(Long idCreneauHoraire) {
        return creneauRepository.findById(idCreneauHoraire).orElse(null);
    }

    @Override
    public void deleteCreneauHoraire(Long idCreneauHoraire) {
        creneauRepository.deleteById(idCreneauHoraire);
    }
}
