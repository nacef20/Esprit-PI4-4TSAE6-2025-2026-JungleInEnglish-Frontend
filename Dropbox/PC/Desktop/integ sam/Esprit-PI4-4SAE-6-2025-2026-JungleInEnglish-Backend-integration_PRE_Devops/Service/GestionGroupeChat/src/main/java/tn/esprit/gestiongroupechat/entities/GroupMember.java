package tn.esprit.gestiongroupechat.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGroupMember ;

    private LocalDateTime joinedAt;

    @Column(length = 36, nullable = false)
    private String userId;

    @ManyToOne
    private GroupChat groupChat;

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }

}
