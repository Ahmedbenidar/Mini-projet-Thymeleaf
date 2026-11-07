package ma.usmba.cinema.modele;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seances")
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date et l’heure sont obligatoires.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(nullable = false)
    private LocalDateTime dateHeure;

    @NotBlank(message = "La salle est obligatoire.")
    @Size(max = 40, message = "Le nom de la salle ne peut dépasser 40 caractères.")
    @Column(nullable = false, length = 40)
    private String salle;

    @NotBlank(message = "La version est obligatoire.")
    @Size(max = 12, message = "La version ne peut dépasser 12 caractères.")
    @Column(nullable = false, length = 12)
    private String version;

    @NotNull(message = "La capacité de la salle est obligatoire.")
    @Min(value = 20, message = "La capacité minimale est de 20 places.")
    @Max(value = 400, message = "La capacité maximale est de 400 places.")
    @Column(nullable = false)
    private Integer capacite;

    @NotNull(message = "Le film est obligatoire.")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "film_id", nullable = false)
    private Film film;

    @OneToMany(mappedBy = "seance")
    private List<Billet> billets = new ArrayList<>();

    public Seance() {
    }

    public Seance(LocalDateTime dateHeure, String salle, String version, Integer capacite, Film film) {
        this.dateHeure = dateHeure;
        this.salle = salle;
        this.version = version;
        this.capacite = capacite;
        this.film = film;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public Film getFilm() {
        return film;
    }

    public void setFilm(Film film) {
        this.film = film;
    }

    public List<Billet> getBillets() {
        return billets;
    }

    public void setBillets(List<Billet> billets) {
        this.billets = billets;
    }
}
