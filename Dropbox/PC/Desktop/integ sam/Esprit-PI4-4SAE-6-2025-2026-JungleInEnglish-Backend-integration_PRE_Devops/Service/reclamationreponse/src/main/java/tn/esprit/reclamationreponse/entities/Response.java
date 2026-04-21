package tn.esprit.reclamationreponse.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "message is required")
    @Size(min = 2, max = 5000, message = "message must contain between 2 and 5000 characters")
    private String message;

    private LocalDateTime createdAt;

    private String attachmentFileName;

    private String attachmentContentType;

    private Long attachmentSize;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] attachmentData;

    @ManyToOne
    @JoinColumn(name = "complaint_id")
    @JsonIgnore
    private Complaint complaint;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
