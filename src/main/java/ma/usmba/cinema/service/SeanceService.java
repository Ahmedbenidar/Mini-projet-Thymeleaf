package ma.usmba.cinema.service;

import ma.usmba.cinema.depot.BilletDepot;
import ma.usmba.cinema.depot.SeanceDepot;
import ma.usmba.cinema.modele.Billet;
import ma.usmba.cinema.modele.Seance;
import ma.usmba.cinema.modele.StatutBillet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SeanceService {

    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm");

    private final SeanceDepot seanceDepot;
    private final BilletDepot billetDepot;

    public SeanceService(SeanceDepot seanceDepot, BilletDepot billetDepot) {
        this.seanceDepot = seanceDepot;
        this.billetDepot = billetDepot;
    }

    public List<Seance> lister(String salle, LocalDateTime jour, Long filmId) {
        boolean aSalle = salle != null && !salle.isBlank();
        boolean aJour = jour != null;
        boolean aFilm = filmId != null;

        List<Seance> resultat = seanceDepot.findAllByOrderByDateHeureAsc();

        if (aFilm) {
            resultat = resultat.stream()
                    .filter(s -> s.getFilm() != null && filmId.equals(s.getFilm().getId()))
                    .toList();
        }
        if (aSalle) {
            String cible = salle.trim();
            resultat = resultat.stream()
                    .filter(s -> s.getSalle() != null && s.getSalle().equalsIgnoreCase(cible))
                    .toList();
        }
        if (aJour) {
            LocalDateTime debut = jour.toLocalDate().atStartOfDay();
            LocalDateTime fin = debut.plusDays(1);
            resultat = resultat.stream()
                    .filter(s -> s.getDateHeure() != null
                            && !s.getDateHeure().isBefore(debut)
                            && s.getDateHeure().isBefore(fin))
                    .toList();
        }
        return resultat;
    }

    public List<Seance> listerAVenir() {
        return seanceDepot.trouverAVenir(LocalDateTime.now().minusMinutes(1));
    }

    public List<String> listerSalles() {
        return seanceDepot.listerSalles();
    }

    public Optional<Seance> trouver(Long id) {
        return seanceDepot.findById(id);
    }

    public Seance trouverOuEchouer(Long id) {
        return seanceDepot.findById(id)
                .orElseThrow(() -> new ExceptionMetier("Aucune séance ne correspond à l'identifiant " + id + "."));
    }

    @Transactional
    public Seance enregistrer(Seance seance) {
        verifierCoherence(seance);
        verifierChevauchement(seance);
        return seanceDepot.save(seance);
    }

    @Transactional
    public Seance modifier(Long id, Seance saisie) {
        Seance existant = trouverOuEchouer(id);
        saisie.setId(id);
        verifierCoherence(saisie);
        verifierChevauchement(saisie);

        long vendues = placesVendues(id);
        if (saisie.getCapacite() != null && saisie.getCapacite() < vendues) {
            throw new ExceptionMetier("La capacité ne peut pas descendre sous les " + vendues
                    + " billet(s) déjà vendus pour cette séance.");
        }

        existant.setDateHeure(saisie.getDateHeure());
        existant.setSalle(saisie.getSalle());
        existant.setVersion(saisie.getVersion());
        existant.setCapacite(saisie.getCapacite());
        existant.setFilm(saisie.getFilm());
        return seanceDepot.save(existant);
    }

    @Transactional
    public void supprimer(Long id) {
        Seance seance = trouverOuEchouer(id);
        long vendues = placesVendues(id);
        if (vendues > 0) {
            throw new ExceptionMetier("Impossible de supprimer cette séance : " + vendues
                    + " billet(s) ont déjà été émis.");
        }
        seanceDepot.delete(seance);
    }

    public long placesVendues(Long seanceId) {
        return billetDepot.countBySeanceIdAndStatutNot(seanceId, StatutBillet.ANNULE);
    }

    public long placesRestantes(Seance seance) {
        if (seance == null || seance.getCapacite() == null) {
            return 0;
        }
        long restantes = seance.getCapacite() - placesVendues(seance.getId());
        return Math.max(restantes, 0);
    }

    public int tauxRemplissage(Seance seance) {
        if (seance == null || seance.getCapacite() == null || seance.getCapacite() == 0) {
            return 0;
        }
        long vendues = placesVendues(seance.getId());
        return (int) Math.round(vendues * 100.0 / seance.getCapacite());
    }

    public long compter() {
        return seanceDepot.count();
    }

    public long compterParFilm(Long filmId) {
        return seanceDepot.countByFilmId(filmId);
    }

    private void verifierCoherence(Seance seance) {
        if (seance.getDateHeure() != null && seance.getDateHeure().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new ExceptionMetier("Impossible de planifier une séance dans le passé.");
        }
        if (seance.getSalle() != null) {
            seance.setSalle(seance.getSalle().trim().toUpperCase());
        }
        if (seance.getVersion() != null) {
            seance.setVersion(seance.getVersion().trim().toUpperCase());
        }
    }

    private void verifierChevauchement(Seance seance) {
        if (seance.getSalle() == null || seance.getDateHeure() == null) {
            return;
        }
        for (Seance autre : seanceDepot.findBySalleIgnoreCaseOrderByDateHeureAsc(seance.getSalle())) {
            if (autre.getId() != null && autre.getId().equals(seance.getId())) {
                continue;
            }
            LocalDateTime debutAutre = autre.getDateHeure();
            if (debutAutre == null) {
                continue;
            }
            int duree = autre.getFilm() != null && autre.getFilm().getDureeMin() != null
                    ? autre.getFilm().getDureeMin() : 90;
            LocalDateTime finAutre = debutAutre.plusMinutes(duree + 20L);
            LocalDateTime debutNouveau = seance.getDateHeure();
            if (debutNouveau.isBefore(finAutre) && debutAutre.isBefore(debutNouveau.plusMinutes(30))) {
                throw new ExceptionMetier("La salle " + seance.getSalle()
                        + " est déjà occupée de " + debutAutre.format(HEURE)
                        + " à " + finAutre.format(HEURE) + " (séance #" + autre.getId() + ").");
            }
        }
    }
}
