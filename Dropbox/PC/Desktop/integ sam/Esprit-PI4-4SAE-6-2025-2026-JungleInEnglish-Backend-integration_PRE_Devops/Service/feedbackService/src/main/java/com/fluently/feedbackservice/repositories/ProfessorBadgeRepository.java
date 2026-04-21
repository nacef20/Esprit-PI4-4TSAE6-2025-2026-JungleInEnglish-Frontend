package com.fluently.feedbackservice.repositories;

import com.fluently.feedbackservice.entities.ProfessorBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorBadgeRepository extends JpaRepository<ProfessorBadge, Long> {
    Optional<ProfessorBadge> findByProfessorId(String professorId);
}
