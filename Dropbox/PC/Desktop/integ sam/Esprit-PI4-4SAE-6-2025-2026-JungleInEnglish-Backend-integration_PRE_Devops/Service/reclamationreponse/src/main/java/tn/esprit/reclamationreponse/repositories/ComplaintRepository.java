package tn.esprit.reclamationreponse.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.reclamationreponse.entities.Complaint;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByStatus(ComplaintStatus status);

    Page<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status, Pageable pageable);

    List<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status);

    List<Complaint> findByTitleContainingIgnoreCase(String title);

    List<Complaint> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

        Page<Complaint> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);

        Page<Complaint> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            ComplaintStatus status,
            String title,
            Pageable pageable
        );

    Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Complaint> findAllByOrderByCreatedAtDesc();
}
