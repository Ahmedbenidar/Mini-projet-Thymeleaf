package ma.usmba.cinema.service;

import ma.usmba.cinema.depot.FilmDepot;
import ma.usmba.cinema.depot.SeanceDepot;
import ma.usmba.cinema.modele.Film;
import ma.usmba.cinema.modele.GenreFilm;
import ma.usmba.cinema.modele.Seance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FilmService {

    private final FilmDepot filmDepot;
    private final SeanceDepot seanceDepot;

    public FilmService(FilmDepot filmDepot, SeanceDepot seanceDepot) {
        this.filmDepot = filmDepot;
        this.seanceDepot = seanceDepot;
    }

    public List<Film> lister(GenreFilm genre, String recherche) {
        boolean aGenre = genre != null;
        boolean aRecherche = recherche != null && !recherche.isBlank();

        if (aGenre && aRecherche) {
            String terme = recherche.trim();
            return filmDepot.findByGenreOrderByTitreAsc(genre).stream()
                    .filter(f -> f.getTitre().toLowerCase().contains(terme.toLowerCase()))
                    .toList();
        }
        if (aGenre) {
            return filmDepot.findByGenreOrderByTitreAsc(genre);
        }
        if (aRecherche) {
            return filmDepot.findByTitreContainingIgnoreCaseOrderByTitreAsc(recherche.trim());
        }
        return filmDepot.findAllByOrderByTitreAsc();
    }

    public Optional<Film> trouver(Long id) {
        return filmDepot.findById(id);
    }

    public Film trouverOuEchouer(Long id) {
        return filmDepot.findById(id)
                .orElseThrow(() -> new ExceptionMetier("Aucun film ne correspond à l'identifiant " + id + "."));
    }

    @Transactional
    public Film enregistrer(Film film) {
        normaliser(film);
        verifierUnicite(film);
        return filmDepot.save(film);
    }

    @Transactional
    public Film modifier(Long id, Film saisie) {
        Film existant = trouverOuEchouer(id);
        normaliser(saisie);
        verifierUnicite(saisie);

        existant.setTitre(saisie.getTitre());
        existant.setGenre(saisie.getGenre());
        existant.setDureeMin(saisie.getDureeMin());
        existant.setRealisateur(saisie.getRealisateur());
        existant.setAnneeSortie(saisie.getAnneeSortie());
        return filmDepot.save(existant);
    }

    @Transactional
    public void supprimer(Long id) {
        Film film = trouverOuEchouer(id);
        List<Seance> seances = seanceDepot.findByFilmIdOrderByDateHeureAsc(id);
        if (!seances.isEmpty()) {
            throw new ExceptionMetier("Impossible de retirer « " + film.getTitre() + " » : "
                    + seances.size() + " séance(s) y sont encore rattachées.");
        }
        filmDepot.delete(film);
    }

    public long compter() {
        return filmDepot.count();
    }

    public long compterRealisateurs() {
        return filmDepot.compterRealisateurs();
    }

    public List<Object[]> repartitionParGenre() {
        return filmDepot.compterParGenre();
    }

    private void normaliser(Film film) {
        if (film.getTitre() != null) {
            film.setTitre(film.getTitre().trim());
        }
        if (film.getRealisateur() != null) {
            film.setRealisateur(film.getRealisateur().trim());
        }
    }

    private void verifierUnicite(Film film) {
        if (film.getTitre() == null || film.getRealisateur() == null) {
            return;
        }
        Optional<Film> doublon = filmDepot.findByTitreIgnoreCase(film.getTitre());
        if (doublon.isPresent() && !doublon.get().getId().equals(film.getId())) {
            throw new ExceptionMetier("Le film « " + film.getTitre() + " » est déjà présent dans le catalogue.");
        }
    }
}
