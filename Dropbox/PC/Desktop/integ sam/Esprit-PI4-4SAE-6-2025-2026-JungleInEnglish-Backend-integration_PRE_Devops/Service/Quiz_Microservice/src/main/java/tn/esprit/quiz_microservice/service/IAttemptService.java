package tn.esprit.quiz_microservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.quiz_microservice.entities.Attempt;

import java.util.List;
import java.util.Optional;

public interface IAttemptService {

    Attempt save(Attempt attempt);

    List<Attempt> findAll();

    Optional<Attempt> findById(Long id);

    Attempt update(Attempt attempt);

    void deleteById(Long id);

    Object[] getStudentStats(Long studentId);

    long countByQuizIdAndStudentId(Long quizId, Long studentId);

    Page<Attempt> findAllPaginated(Pageable pageable);

    Page<Attempt> findByQuizIdPaginated(Long quizId, Pageable pageable);

    Page<Attempt> findByStudentIdPaginated(Long studentId, Pageable pageable);

    Page<Attempt> findByQuizIdAndStudentIdPaginated(Long quizId, Long studentId, Pageable pageable);

    Page<Attempt> findByQuizTitlePaginated(String title, Pageable pageable);

    Page<Attempt> findByQuizTitleAndStudentIdPaginated(String title, Long studentId, Pageable pageable);

    Page<Attempt> findByStudentNamePaginated(String name, Pageable pageable);

    Page<Attempt> findByStudentNameAndQuizIdPaginated(String name, Long quizId, Pageable pageable);

    Page<Attempt> findByQuizTitleAndStudentNamePaginated(String title, String name, Pageable pageable);
}
