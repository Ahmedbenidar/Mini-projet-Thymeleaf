package ma.usmba.cinema.modele;

public enum GenreFilm {

    ACTION("Action"),
    COMEDIE("Comédie"),
    DRAME("Drame"),
    SCIENCE_FICTION("Science-fiction"),
    ANIMATION("Animation"),
    DOCUMENTAIRE("Documentaire");

    private final String libelle;

    GenreFilm(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
