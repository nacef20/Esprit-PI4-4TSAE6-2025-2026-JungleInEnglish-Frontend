package tn.esprit.quiz_microservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String certificateNumber;

    @OneToOne
    @JoinColumn(name = "attempt_id", unique = true, nullable = false)
    @JsonIgnoreProperties({"choices"})
    private Attempt attempt;

    private String studentName;
    private String quizTitle;
    private Float score;
    private Integer totalPoints;
    private Integer percentage;
    private String grade;

    private LocalDateTime issuedAt;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] pdfData;
}
