package tn.esprit.forumservice.entites;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity

@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "post_id"})
})
public class Reaction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReaction;


    @Column(nullable = false)
    private String userId;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReactionType type;

    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @JsonBackReference
    private Post post;



    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
