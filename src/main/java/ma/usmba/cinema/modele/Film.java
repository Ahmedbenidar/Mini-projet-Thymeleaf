package ma.usmba.cinema.modele;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "films")
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire.")
    @Size(min = 2, max = 120, message = "Le titre doit contenir entre 2 et 120 caractères.")
    @Column(nullable = false, length = 120)
    private String titre;

    @NotNull(message = "Le genre est obligatoire.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private GenreFilm genre;

    @NotNull(message = "La durée est obligatoire.")
    @Min(value = 20, message = "La durée minimale est de 20 minutes.")
    @Max(value = 300, message = "La durée maximale est de 300 minutes.")
    @Column(nullable = false)
    private Integer dureeMin;

    @NotBlank(message = "Le réalisateur est obligatoire.")
    @Size(max = 90, message = "Le nom du réalisateur ne peut dépasser 90 caractères.")
    @Column(nullable = false, length = 90)
    private String realisateur;

    @NotNull(message = "L’année de sortie est obligatoire.")
    @Min(value = 1900, message = "L’année de sortie doit être postérieure à 1900.")
    @Max(value = 2100, message = "L’année de sortie est invalide.")
    @Column(nullable = false)
    private Integer anneeSortie;

    @OneToMany(mappedBy = "film")
    private List<Seance> seances = new ArrayList<>();

    public Film() {
    }

    public Film(String titre, GenreFilm genre, Integer dureeMin, String realisateur, Integer anneeSortie) {
        this.titre = titre;
        this.genre = genre;
        this.dureeMin = dureeMin;
        this.realisateur = realisateur;
        this.anneeSortie = anneeSortie;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public GenreFilm getGenre() {
        return genre;
    }

    public void setGenre(GenreFilm genre) {
        this.genre = genre;
    }

    public Integer getDureeMin() {
        return dureeMin;
    }

    public void setDureeMin(Integer dureeMin) {
        this.dureeMin = dureeMin;
    }

    public String getRealisateur() {
        return realisateur;
    }

    public void setRealisateur(String realisateur) {
        this.realisateur = realisateur;
    }

    public Integer getAnneeSortie() {
        return anneeSortie;
    }

    public void setAnneeSortie(Integer anneeSortie) {
        this.anneeSortie = anneeSortie;
    }

    public List<Seance> getSeances() {
        return seances;
    }

    public void setSeances(List<Seance> seances) {
        this.seances = seances;
    }

    public String getDureeFormatee() {
        if (dureeMin == null) {
            return "-";
        }
        return (dureeMin / 60) + "h" + String.format("%02d", dureeMin % 60);
    }
}
