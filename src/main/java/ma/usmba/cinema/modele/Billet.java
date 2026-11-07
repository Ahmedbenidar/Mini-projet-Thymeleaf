package ma.usmba.cinema.modele;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billets")
public class Billet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le prix est obligatoire.")
    @DecimalMin(value = "10.00", message = "Le prix minimum est de 10 MAD.")
    @DecimalMax(value = "500.00", message = "Le prix maximum est de 500 MAD.")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal prix;

    @NotNull(message = "Le statut est obligatoire.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatutBillet statut = StatutBillet.VALIDE;

    @NotNull(message = "La date d’achat est obligatoire.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(nullable = false)
    private LocalDateTime dateAchat;

    @NotBlank(message = "Le nom de l’acheteur est obligatoire.")
    @Size(max = 90, message = "Le nom de l’acheteur ne peut dépasser 90 caractères.")
    @Column(nullable = false, length = 90)
    private String acheteur;

    @NotBlank(message = "Le téléphone de l’acheteur est obligatoire.")
    @Pattern(regexp = "0[5-7][0-9]{8}", message = "Le téléphone doit contenir 10 chiffres et commencer par 05, 06 ou 07.")
    @Column(nullable = false, length = 10)
    private String telephone;

    @NotNull(message = "La séance est obligatoire.")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seance_id", nullable = false)
    private Seance seance;

    public Billet() {
    }

    public Billet(BigDecimal prix, StatutBillet statut, LocalDateTime dateAchat, String acheteur, String telephone, Seance seance) {
        this.prix = prix;
        this.statut = statut;
        this.dateAchat = dateAchat;
        this.acheteur = acheteur;
        this.telephone = telephone;
        this.seance = seance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public StatutBillet getStatut() {
        return statut;
    }

    public void setStatut(StatutBillet statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateAchat() {
        return dateAchat;
    }

    public void setDateAchat(LocalDateTime dateAchat) {
        this.dateAchat = dateAchat;
    }

    public String getAcheteur() {
        return acheteur;
    }

    public void setAcheteur(String acheteur) {
        this.acheteur = acheteur;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Seance getSeance() {
        return seance;
    }

    public void setSeance(Seance seance) {
        this.seance = seance;
    }

    public String getClasseStatut() {
        if (statut == null) {
            return "statut-neutre";
        }
        return switch (statut) {
            case VALIDE -> "statut-valide";
            case UTILISE -> "statut-utilise";
            case ANNULE -> "statut-annule";
        };
    }
}
