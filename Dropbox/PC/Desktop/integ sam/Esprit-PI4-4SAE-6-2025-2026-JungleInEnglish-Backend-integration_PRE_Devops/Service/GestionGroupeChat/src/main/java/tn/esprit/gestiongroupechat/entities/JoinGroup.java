package tn.esprit.gestiongroupechat.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class JoinGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long idJoinGroup;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private  Status status;
    private LocalDateTime requestedAt;
    @Column(length = 36)
    private String userId;
    @ManyToOne
    @JsonIgnore
    private GroupChat groupChat;

    @PrePersist
    public void prePersist() {
        this.requestedAt = LocalDateTime.now();
    }

}
