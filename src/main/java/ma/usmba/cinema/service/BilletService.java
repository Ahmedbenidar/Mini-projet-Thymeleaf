package ma.usmba.cinema.service;

import ma.usmba.cinema.depot.BilletDepot;
import ma.usmba.cinema.modele.Billet;
import ma.usmba.cinema.modele.Seance;
import ma.usmba.cinema.modele.StatutBillet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BilletService {

    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm");

    private final BilletDepot billetDepot;
    private final SeanceService seanceService;

    public BilletService(BilletDepot billetDepot, SeanceService seanceService) {
        this.billetDepot = billetDepot;
        this.seanceService = seanceService;
    }

    public List<Billet> lister(StatutBillet statut, String recherche) {
        boolean aStatut = statut != null;
        boolean aRecherche = recherche != null && !recherche.isBlank();

        List<Billet> resultat = aStatut
                ? billetDepot.findByStatutOrderByDateAchatDesc(statut)
                : billetDepot.findAllByOrderByDateAchatDesc();

        if (aRecherche) {
            String terme = recherche.trim().toLowerCase();
            resultat = resultat.stream()
                    .filter(b -> (b.getAcheteur() != null && b.getAcheteur().toLowerCase().contains(terme))
                            || (b.getTelephone() != null && b.getTelephone().contains(terme)))
                    .toList();
        }
        return resultat;
    }

    public Optional<Billet> trouver(Long id) {
        return billetDepot.findById(id);
    }

    public Billet trouverOuEchouer(Long id) {
        return billetDepot.findById(id)
                .orElseThrow(() -> new ExceptionMetier("Aucun billet ne correspond à l'identifiant " + id + "."));
    }

    @Transactional
    public Billet vendre(Billet billet) {
        if (billet.getSeance() == null || billet.getSeance().getId() == null) {
            throw new ExceptionMetier("Veuillez choisir une séance.");
        }
        Seance seance = seanceService.trouverOuEchouer(billet.getSeance().getId());
        billet.setSeance(seance);

        if (seance.getDateHeure() != null && seance.getDateHeure().isBefore(LocalDateTime.now())) {
            throw new ExceptionMetier("La séance du " + seance.getDateHeure().format(JOUR)
                    + " à " + seance.getDateHeure().format(HEURE) + " est déjà passée.");
        }
        if (seanceService.placesRestantes(seance) <= 0) {
            throw new ExceptionMetier("La séance est complète (" + seance.getCapacite() + "/"
                    + seance.getCapacite() + " places vendues).");
        }

        if (billet.getAcheteur() != null) {
            billet.setAcheteur(billet.getAcheteur().trim());
        }
        if (billet.getTelephone() != null) {
            billet.setTelephone(billet.getTelephone().trim());
        }
        if (billet.getDateAchat() == null) {
            billet.setDateAchat(LocalDateTime.now());
        }
        if (billet.getStatut() == null) {
            billet.setStatut(StatutBillet.VALIDE);
        }
        return billetDepot.save(billet);
    }

    @Transactional
    public void annuler(Long id) {
        Billet billet = trouverOuEchouer(id);
        if (billet.getStatut() == StatutBillet.ANNULE) {
            throw new ExceptionMetier("Le billet #" + id + " est déjà annulé.");
        }
        if (billet.getStatut() == StatutBillet.UTILISE) {
            throw new ExceptionMetier("Le billet #" + id + " a déjà été utilisé, il ne peut plus être annulé.");
        }
        billet.setStatut(StatutBillet.ANNULE);
        billetDepot.save(billet);
    }

    @Transactional
    public void marquerUtilise(Long id) {
        Billet billet = trouverOuEchouer(id);
        if (billet.getStatut() != StatutBillet.VALIDE) {
            throw new ExceptionMetier("Seul un billet valide peut être contrôlé à l'entrée.");
        }
        billet.setStatut(StatutBillet.UTILISE);
        billetDepot.save(billet);
    }

    @Transactional
    public Billet modifier(Long id, Billet saisie) {
        Billet existant = trouverOuEchouer(id);
        existant.setAcheteur(saisie.getAcheteur() == null ? existant.getAcheteur() : saisie.getAcheteur().trim());
        existant.setTelephone(saisie.getTelephone() == null ? existant.getTelephone() : saisie.getTelephone().trim());
        existant.setPrix(saisie.getPrix());
        existant.setStatut(saisie.getStatut());
        return billetDepot.save(existant);
    }

    @Transactional
    public void supprimer(Long id) {
        Billet billet = trouverOuEchouer(id);
        billetDepot.delete(billet);
    }

    public long compter() {
        return billetDepot.count();
    }

    public long compterParStatut(StatutBillet statut) {
        return billetDepot.countByStatut(statut);
    }

    public BigDecimal chiffreAffaires() {
        BigDecimal total = billetDepot.chiffreAffairesTotal(StatutBillet.ANNULE);
        return total == null ? BigDecimal.ZERO : total;
    }

    public List<Object[]> recettesParFilm() {
        return billetDepot.recettesParFilm(StatutBillet.ANNULE);
    }

    public List<Object[]> ventesParMois() {
        return billetDepot.ventesParMois(StatutBillet.ANNULE).stream()
                .map(ligne -> new Object[] { libelleMois(String.valueOf(ligne[0])), ligne[1], ligne[2] })
                .toList();
    }

    private String libelleMois(String cle) {
        try {
            YearMonth mois = YearMonth.parse(cle);
            String nom = mois.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
            return nom.substring(0, 1).toUpperCase(Locale.FRENCH) + nom.substring(1) + " " + mois.getYear();
        } catch (DateTimeParseException e) {
            return cle;
        }
    }

    public long totalPlacesVendues() {
        return billetDepot.count() - billetDepot.countByStatut(StatutBillet.ANNULE);
    }
}
