package tn.esprit.forumservice.entites;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String content;
    private String authorid;

    private LocalDateTime dateSend;


    @ManyToOne
    @JoinColumn(name = "post_idpost")
    @JsonBackReference
    private Post post;

    @PrePersist
    public void prePersist() {
        this.dateSend = LocalDateTime.now();
    }
}
