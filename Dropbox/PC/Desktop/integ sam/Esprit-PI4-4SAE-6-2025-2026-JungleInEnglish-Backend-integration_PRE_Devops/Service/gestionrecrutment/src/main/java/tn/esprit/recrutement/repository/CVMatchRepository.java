package tn.esprit.recrutement.repository;

import tn.esprit.recrutement.model.CVMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CVMatchRepository extends JpaRepository<CVMatch, Long> {

    @Query("SELECT DISTINCT m.jobOffer.id FROM CVMatch m WHERE m.cv.user.id = :userId")
    List<Long> findJobOfferIdsByCandidateId(@Param("userId") Integer userId);

    @Query("SELECT m FROM CVMatch m JOIN FETCH m.cv JOIN FETCH m.jobOffer WHERE m.cv.user.id = :userId")
    List<CVMatch> findByCandidateIdWithFetch(@Param("userId") Integer userId);

    List<CVMatch> findByStatus(String status);

    List<CVMatch> findByJobOfferId(Long jobOfferId);

    @Modifying
    @Transactional
    void deleteByJobOfferId(Long jobOfferId);
}