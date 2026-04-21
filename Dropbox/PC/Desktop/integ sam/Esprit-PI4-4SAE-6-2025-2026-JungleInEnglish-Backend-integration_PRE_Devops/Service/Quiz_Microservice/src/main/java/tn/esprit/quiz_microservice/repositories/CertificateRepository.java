package tn.esprit.quiz_microservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.quiz_microservice.entities.Certificate;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByAttemptId(Long attemptId);

    boolean existsByAttemptId(Long attemptId);
}
