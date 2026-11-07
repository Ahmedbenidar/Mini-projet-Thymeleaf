package ma.usmba.cinema.modele;

public enum StatutBillet {

    VALIDE("Valide"),
    UTILISE("Utilisé"),
    ANNULE("Annulé");

    private final String libelle;

    StatutBillet(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
