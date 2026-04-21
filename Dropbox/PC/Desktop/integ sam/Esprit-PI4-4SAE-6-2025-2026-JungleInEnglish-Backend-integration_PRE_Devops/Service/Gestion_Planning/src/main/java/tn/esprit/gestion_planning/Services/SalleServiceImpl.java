package tn.esprit.gestion_planning.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_planning.Entites.Salle;
import tn.esprit.gestion_planning.Repositories.SalleRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class SalleServiceImpl implements ISalleService {

    final SalleRepository salleRepository;

    @Override
    public Salle addSalle(Salle salle) {
        return salleRepository.save(salle);
    }

    @Override
    public Salle updateSalle(Salle salle) {
        return salleRepository.save(salle);
    }

    @Override
    public List<Salle> getAllSalles() {
        return salleRepository.findAll();
    }

    @Override
    public Salle getSalleById(Long idSalle) {
        return salleRepository.findById(idSalle).orElse(null);
    }

    @Override
    public void deleteSalle(Long idSalle) {
        salleRepository.deleteById(idSalle);
    }
}