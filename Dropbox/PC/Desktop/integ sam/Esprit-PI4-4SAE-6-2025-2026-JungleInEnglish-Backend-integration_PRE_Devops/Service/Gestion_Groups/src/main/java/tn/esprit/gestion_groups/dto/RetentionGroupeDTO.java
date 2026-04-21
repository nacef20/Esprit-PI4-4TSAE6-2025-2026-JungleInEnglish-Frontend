package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RetentionGroupeDTO {

    private Long idGroupe;
    private String nomGroupe;
    private long totalAffectations;
    private double dureeMoyenneJours;
    private long nombreSortis;
    private long nombreSuspendus;
    private long nombreActifs;
    private double tauxRetention;    // (actifs / total) * 100
}
