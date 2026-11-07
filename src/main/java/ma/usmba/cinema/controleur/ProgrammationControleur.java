package ma.usmba.cinema.controleur;

import jakarta.validation.Valid;
import ma.usmba.cinema.modele.Billet;
import ma.usmba.cinema.modele.Film;
import ma.usmba.cinema.modele.GenreFilm;
import ma.usmba.cinema.modele.Seance;
import ma.usmba.cinema.modele.StatutBillet;
import ma.usmba.cinema.service.BilletService;
import ma.usmba.cinema.service.ExceptionMetier;
import ma.usmba.cinema.service.FilmService;
import ma.usmba.cinema.service.SeanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class ProgrammationControleur {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    private final FilmService filmService;
    private final SeanceService seanceService;
    private final BilletService billetService;

    public ProgrammationControleur(FilmService filmService, SeanceService seanceService, BilletService billetService) {
        this.filmService = filmService;
        this.seanceService = seanceService;
        this.billetService = billetService;
    }

    @ModelAttribute("genres")
    public GenreFilm[] genres() {
        return GenreFilm.values();
    }

    @ModelAttribute("statuts")
    public StatutBillet[] statuts() {
        return StatutBillet.values();
    }

    /* ----------------------------- Tableau de bord ----------------------------- */

    @GetMapping
    public String tableauDeBord(Model modele) {
        List<Seance> prochaines = seanceService.listerAVenir();
        if (prochaines.size() > 6) {
            prochaines = prochaines.subList(0, 6);
        }

        modele.addAttribute("nombreFilms", filmService.compter());
        modele.addAttribute("nombreSeances", seanceService.compter());
        modele.addAttribute("nombreBillets", billetService.compter());
        modele.addAttribute("chiffreAffaires", billetService.chiffreAffaires());
        modele.addAttribute("nombreRealisateurs", filmService.compterRealisateurs());
        modele.addAttribute("billetsValides", billetService.compterParStatut(StatutBillet.VALIDE));
        modele.addAttribute("billetsAnnules", billetService.compterParStatut(StatutBillet.ANNULE));
        modele.addAttribute("prochainesSeances", prochaines);
        modele.addAttribute("remplissage", prochaines.stream()
                .map(s -> seanceService.tauxRemplissage(s))
                .toList());
        modele.addAttribute("recettesParFilm", billetService.recettesParFilm());
        modele.addAttribute("repartitionGenres", filmService.repartitionParGenre());
        modele.addAttribute("pageActive", "tableau-de-bord");
        return "tableau-de-bord";
    }

    /* ---------------------------------- Films --------------------------------- */

    @GetMapping("/films")
    public String listeFilms(@RequestParam(required = false) GenreFilm genre,
                             @RequestParam(required = false) String recherche,
                             Model modele) {
        List<Film> films = filmService.lister(genre, recherche);
        Map<Long, Long> seancesParFilm = new HashMap<>();
        for (Film film : films) {
            seancesParFilm.put(film.getId(), seanceService.compterParFilm(film.getId()));
        }
        modele.addAttribute("films", films);
        modele.addAttribute("seancesParFilm", seancesParFilm);
        modele.addAttribute("genreChoisi", genre);
        modele.addAttribute("recherche", recherche == null ? "" : recherche);
        modele.addAttribute("pageActive", "films");
        return "liste-films";
    }

    @GetMapping("/films/nouveau")
    public String formulaireNouveauFilm(Model modele) {
        modele.addAttribute("film", new Film());
        modele.addAttribute("modeEdition", false);
        modele.addAttribute("pageActive", "films");
        return "formulaire-film";
    }

    @PostMapping("/films/nouveau")
    public String creerFilm(@Valid @ModelAttribute("film") Film film,
                            BindingResult resultats,
                            Model modele,
                            RedirectAttributes flash) {
        if (resultats.hasErrors()) {
            modele.addAttribute("modeEdition", false);
            modele.addAttribute("pageActive", "films");
            return "formulaire-film";
        }
        try {
            filmService.enregistrer(film);
        } catch (ExceptionMetier ex) {
            modele.addAttribute("modeEdition", false);
            modele.addAttribute("pageActive", "films");
            modele.addAttribute("erreurMetier", ex.getMessage());
            return "formulaire-film";
        }
        flash.addFlashAttribute("succes", "Le film « " + film.getTitre() + " » a été ajouté au catalogue.");
        return "redirect:/films";
    }

    @GetMapping("/films/{id}/modifier")
    public String formulaireEditionFilm(@PathVariable Long id, Model modele, RedirectAttributes flash) {
        return filmService.trouver(id).map(film -> {
            modele.addAttribute("film", film);
            modele.addAttribute("modeEdition", true);
            modele.addAttribute("pageActive", "films");
            return "formulaire-film";
        }).orElseGet(() -> {
            flash.addFlashAttribute("erreur", "Ce film n'existe pas.");
            return "redirect:/films";
        });
    }

    @PostMapping("/films/{id}/modifier")
    public String modifierFilm(@PathVariable Long id,
                               @Valid @ModelAttribute("film") Film film,
                               BindingResult resultats,
                               Model modele,
                               RedirectAttributes flash) {
        if (resultats.hasErrors()) {
            modele.addAttribute("modeEdition", true);
            modele.addAttribute("pageActive", "films");
            return "formulaire-film";
        }
        try {
            filmService.modifier(id, film);
        } catch (ExceptionMetier ex) {
            modele.addAttribute("modeEdition", true);
            modele.addAttribute("pageActive", "films");
            modele.addAttribute("erreurMetier", ex.getMessage());
            return "formulaire-film";
        }
        flash.addFlashAttribute("succes", "La fiche du film a été mise à jour.");
        return "redirect:/films";
    }

    @PostMapping("/films/{id}/supprimer")
    public String supprimerFilm(@PathVariable Long id, RedirectAttributes flash) {
        try {
            String titre = filmService.trouverOuEchouer(id).getTitre();
            filmService.supprimer(id);
            flash.addFlashAttribute("succes", "Le film « " + titre + " » a été retiré du catalogue.");
        } catch (ExceptionMetier ex) {
            flash.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/films";
    }

    /* --------------------------------- Seances -------------------------------- */

    @GetMapping("/seances")
    public String listeSeances(@RequestParam(required = false) String salle,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour,
                               @RequestParam(required = false) Long filmId,
                               Model modele) {
        List<Seance> seances = seanceService.lister(salle, jour == null ? null : jour.atStartOfDay(), filmId);
        modele.addAttribute("seances", seances);
        modele.addAttribute("salles", seanceService.listerSalles());
        modele.addAttribute("tousLesFilms", filmService.lister(null, null));
        modele.addAttribute("salleChoisie", salle == null ? "" : salle);
        modele.addAttribute("jourChoisi", jour);
        modele.addAttribute("filmChoisi", filmId);
        modele.addAttribute("placesRestantes", seances.stream()
                .map(s -> seanceService.placesRestantes(s))
                .toList());
        modele.addAttribute("remplissage", seances.stream()
                .map(s -> seanceService.tauxRemplissage(s))
                .toList());
        modele.addAttribute("pageActive", "seances");
        return "liste-seances";
    }

    @GetMapping("/seances/nouvelle")
    public String formulaireNouvelleSeance(Model modele) {
        Seance seance = new Seance();
        seance.setDateHeure(LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0));
        seance.setVersion("VF");
        seance.setCapacite(120);
        modele.addAttribute("seance", seance);
        modele.addAttribute("tousLesFilms", filmService.lister(null, null));
        modele.addAttribute("modeEdition", false);
        modele.addAttribute("pageActive", "seances");
        return "formulaire-seance";
    }

    @PostMapping("/seances/nouvelle")
    public String creerSeance(@Valid @ModelAttribute("seance") Seance seance,
                              BindingResult resultats,
                              Model modele,
                              RedirectAttributes flash) {
        if (resultats.hasErrors()) {
            return formulaireSeanceEnErreur(modele, false, null);
        }
        try {
            seanceService.enregistrer(seance);
        } catch (ExceptionMetier ex) {
            return formulaireSeanceEnErreur(modele, false, ex.getMessage());
        }
        flash.addFlashAttribute("succes", "La séance du " + FORMAT_DATE.format(seance.getDateHeure())
                + " à " + FORMAT_HEURE.format(seance.getDateHeure()) + " a été planifiée en salle " + seance.getSalle() + ".");
        return "redirect:/seances";
    }

    @GetMapping("/seances/{id}/modifier")
    public String formulaireEditionSeance(@PathVariable Long id, Model modele, RedirectAttributes flash) {
        return seanceService.trouver(id).map(seance -> {
            modele.addAttribute("seance", seance);
            modele.addAttribute("tousLesFilms", filmService.lister(null, null));
            modele.addAttribute("modeEdition", true);
            modele.addAttribute("pageActive", "seances");
            return "formulaire-seance";
        }).orElseGet(() -> {
            flash.addFlashAttribute("erreur", "Cette séance n'existe pas.");
            return "redirect:/seances";
        });
    }

    @PostMapping("/seances/{id}/modifier")
    public String modifierSeance(@PathVariable Long id,
                                 @Valid @ModelAttribute("seance") Seance seance,
                                 BindingResult resultats,
                                 Model modele,
                                 RedirectAttributes flash) {
        if (resultats.hasErrors()) {
            return formulaireSeanceEnErreur(modele, true, null);
        }
        try {
            seanceService.modifier(id, seance);
        } catch (ExceptionMetier ex) {
            return formulaireSeanceEnErreur(modele, true, ex.getMessage());
        }
        flash.addFlashAttribute("succes", "La séance a été mise à jour.");
        return "redirect:/seances";
    }

    @PostMapping("/seances/{id}/supprimer")
    public String supprimerSeance(@PathVariable Long id, RedirectAttributes flash) {
        try {
            seanceService.supprimer(id);
            flash.addFlashAttribute("succes", "La séance a été annulée de la programmation.");
        } catch (ExceptionMetier ex) {
            flash.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/seances";
    }

    private String formulaireSeanceEnErreur(Model modele, boolean modeEdition, String erreurMetier) {
        modele.addAttribute("tousLesFilms", filmService.lister(null, null));
        modele.addAttribute("modeEdition", modeEdition);
        modele.addAttribute("pageActive", "seances");
        if (erreurMetier != null) {
            modele.addAttribute("erreurMetier", erreurMetier);
        }
        return "formulaire-seance";
    }

    /* --------------------------------- Billets -------------------------------- */

    @GetMapping("/billets")
    public String listeBillets(@RequestParam(required = false) StatutBillet statut,
                               @RequestParam(required = false) String recherche,
                               Model modele) {
        modele.addAttribute("billets", billetService.lister(statut, recherche));
        modele.addAttribute("statutChoisi", statut);
        modele.addAttribute("recherche", recherche == null ? "" : recherche);
        modele.addAttribute("recetteTotale", billetService.chiffreAffaires());
        modele.addAttribute("pageActive", "billets");
        return "liste-billets";
    }

    @GetMapping("/billets/nouveau")
    public String formulaireNouveauBillet(@RequestParam(required = false) Long seanceId, Model modele) {
        Billet billet = new Billet();
        billet.setPrix(new BigDecimal("45.00"));
        billet.setStatut(StatutBillet.VALIDE);
        billet.setDateAchat(LocalDateTime.now().withSecond(0).withNano(0));
        if (seanceId != null) {
            seanceService.trouver(seanceId).ifPresent(billet::setSeance);
        }
        preparerFormulaireBillet(modele, billet);
        return "formulaire-billet";
    }

    @PostMapping("/billets/nouveau")
    public String vendreBillet(@Valid @ModelAttribute("billet") Billet billet,
                               BindingResult resultats,
                               Model modele,
                               RedirectAttributes flash) {
        if (resultats.hasErrors()) {
            preparerFormulaireBillet(modele, billet);
            return "formulaire-billet";
        }
        try {
            billetService.vendre(billet);
        } catch (ExceptionMetier ex) {
            preparerFormulaireBillet(modele, billet);
            modele.addAttribute("erreurMetier", ex.getMessage());
            return "formulaire-billet";
        }
        flash.addFlashAttribute("succes", "Le billet de " + billet.getAcheteur()
                + " a été émis pour la séance « " + billet.getSeance().getFilm().getTitre() + " ».");
        return "redirect:/billets";
    }

    private void preparerFormulaireBillet(Model modele, Billet billet) {
        modele.addAttribute("billet", billet);
        modele.addAttribute("seancesDisponibles", seanceService.listerAVenir());
        modele.addAttribute("pageActive", "billets");
    }

    @PostMapping("/billets/{id}/annuler")
    public String annulerBillet(@PathVariable Long id, RedirectAttributes flash) {
        try {
            billetService.annuler(id);
            flash.addFlashAttribute("succes", "Le billet #" + id + " a été annulé, la place est remise en vente.");
        } catch (ExceptionMetier ex) {
            flash.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/billets";
    }

    @PostMapping("/billets/{id}/valider")
    public String validerBillet(@PathVariable Long id, RedirectAttributes flash) {
        try {
            billetService.marquerUtilise(id);
            flash.addFlashAttribute("succes", "Le billet #" + id + " est marqué comme utilisé.");
        } catch (ExceptionMetier ex) {
            flash.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/billets";
    }

    @PostMapping("/billets/{id}/supprimer")
    public String supprimerBillet(@PathVariable Long id, RedirectAttributes flash) {
        try {
            billetService.supprimer(id);
            flash.addFlashAttribute("succes", "Le billet #" + id + " a été supprimé du registre.");
        } catch (ExceptionMetier ex) {
            flash.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/billets";
    }

    /* ------------------------------ Statistiques ------------------------------ */

    @GetMapping("/statistiques")
    public String statistiques(Model modele) {
        List<Seance> seances = seanceService.lister(null, null, null);
        modele.addAttribute("seances", seances);
        modele.addAttribute("remplissage", seances.stream()
                .map(s -> seanceService.tauxRemplissage(s))
                .toList());
        modele.addAttribute("placesVendues", seances.stream()
                .map(s -> seanceService.placesVendues(s.getId()))
                .toList());
        modele.addAttribute("recettesParFilm", billetService.recettesParFilm());
        modele.addAttribute("ventesParMois", billetService.ventesParMois());
        modele.addAttribute("repartitionGenres", filmService.repartitionParGenre());
        modele.addAttribute("chiffreAffaires", billetService.chiffreAffaires());
        modele.addAttribute("pageActive", "statistiques");
        return "statistiques";
    }
}
