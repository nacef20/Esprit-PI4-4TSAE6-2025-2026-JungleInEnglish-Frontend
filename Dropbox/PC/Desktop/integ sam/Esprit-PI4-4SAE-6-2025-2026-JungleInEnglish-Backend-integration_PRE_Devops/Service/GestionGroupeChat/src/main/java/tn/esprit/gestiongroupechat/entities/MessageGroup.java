package tn.esprit.gestiongroupechat.entities;

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
public class MessageGroup {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

      private Long idMessageChat;
      @Column(length = 36)
      private String sender;
      private String content;
      private LocalDateTime dateSend;
    private boolean deleted = false;
    @ManyToOne
    private GroupChat groupChat;

    @PrePersist
    public void prePersist() {
        this.dateSend = LocalDateTime.now();
    }

}
