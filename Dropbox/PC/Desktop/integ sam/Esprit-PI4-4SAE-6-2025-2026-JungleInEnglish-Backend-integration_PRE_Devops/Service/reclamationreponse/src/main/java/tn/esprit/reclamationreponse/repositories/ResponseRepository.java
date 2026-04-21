package tn.esprit.reclamationreponse.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.reclamationreponse.entities.Response;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {

    List<Response> findByComplaintId(Long complaintId);

    List<Response> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);

    List<Response> findAllByOrderByComplaintIdAscCreatedAtAsc();
}
