package tn.esprit.forumservice.service;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public interface PostScoreProjection {

    Long getIdpost();
    String getAuthorid();
    Long getScore();
}
