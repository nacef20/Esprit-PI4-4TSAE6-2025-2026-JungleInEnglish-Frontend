package tn.esprit.gestion_groups.dto;

import lombok.Getter;

@Getter
public class ScoreSanteGroupeDTO {

    private Long idGroupe;
    private String nom;
    private String codeGroupe;

    // Composantes du score (chacune sur 25 pts)
    private Double scoreTauxRemplissage;   // capaciteActuelle / capaciteMax
    private Double scoreObjectifs;          // % objectifs ATTEINT
    private Double scoreReglements;         // % règlements obligatoires actifs
    private Double scoreStabilite;          // pénalité par changement récent

    private Double scoreTotal;              // somme des 4 composantes (0-100)
    private String niveauSante;             // CRITIQUE / FAIBLE / MOYEN / BON

    public ScoreSanteGroupeDTO(Long idGroupe, String nom, String codeGroupe,
                                Double scoreTauxRemplissage, Double scoreObjectifs,
                                Double scoreReglements, Double scoreStabilite,
                                Double scoreTotal) {
        this.idGroupe = idGroupe;
        this.nom = nom;
        this.codeGroupe = codeGroupe;
        this.scoreTauxRemplissage = round(scoreTauxRemplissage);
        this.scoreObjectifs = round(scoreObjectifs);
        this.scoreReglements = round(scoreReglements);
        this.scoreStabilite = round(scoreStabilite);
        this.scoreTotal = round(scoreTotal);
        this.niveauSante = determinerNiveau(scoreTotal);
    }

    private static Double round(Double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private static String determinerNiveau(Double score) {
        if (score < 40) return "CRITIQUE";
        if (score < 60) return "FAIBLE";
        if (score < 80) return "MOYEN";
        return "BON";
    }
}
