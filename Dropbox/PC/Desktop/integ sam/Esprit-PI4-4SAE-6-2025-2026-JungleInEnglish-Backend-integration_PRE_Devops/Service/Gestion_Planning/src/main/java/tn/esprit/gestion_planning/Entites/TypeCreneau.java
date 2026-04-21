package tn.esprit.gestion_planning.Entites;

public enum TypeCreneau {
    MATIN("08h-12h"),
    APRES_MIDI("14h-18h"),
    SOIREE("18h-22h");

    private String plageHoraire;

    TypeCreneau(String plageHoraire) {
        this.plageHoraire = plageHoraire;
    }
}