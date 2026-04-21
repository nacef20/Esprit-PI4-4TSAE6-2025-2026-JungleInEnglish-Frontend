package tn.esprit.recrutement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "user")
    private User user; // ✅ Keycloak user ID

    @Column(columnDefinition = "TEXT")
    private String content;

    private String fileName;
    private String fileType;
    private Date uploadDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cv_skills")
    private Set<String> skills;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cv_experience")
    private Set<String> experiences;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cv_education")
    private Set<String> education;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cv_languages")
    private Set<String> languages;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cv_certifications")
    private Set<String> certifications;
}
