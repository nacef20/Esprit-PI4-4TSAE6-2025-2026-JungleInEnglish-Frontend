package tn.esprit.gestiongroupechat.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GroupChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    @Column(unique = true , nullable = false)
    private String name;
    @Column(length = 36)
    private String ownerId;
    private LocalDateTime creationDate;
    private String image;



    @OneToMany(mappedBy = "groupChat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set <JoinGroup> joinGroups;

    @OneToMany( mappedBy = "groupChat", cascade = CascadeType.ALL , fetch = FetchType.LAZY)

    private Set<GroupMember> members;
    @OneToMany(mappedBy = "groupChat",cascade = CascadeType.ALL , fetch = FetchType.LAZY)
    private Set <MessageGroup> messageGroups;


    @PrePersist
    public void prePersist() {
        this.creationDate = LocalDateTime.now();
    }
}
