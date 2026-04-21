package tn.esprit.gestion_planning.Entites;

public enum Equipement {
    VIDEOPROJECTEUR("Vidéo projecteur"),
    TABLEAU_BLANC("Tableau blanc interactif"),
    WIFI("Connexion Wi-Fi"),
    ORDINATEUR("Poste informatique"),
    CLIMATISATION("Climatisation");

    private String libelle;

    Equipement(String libelle) {
        this.libelle = libelle;
    }
}
