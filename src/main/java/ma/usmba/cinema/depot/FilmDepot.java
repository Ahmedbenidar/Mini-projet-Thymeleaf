package ma.usmba.cinema.depot;

import ma.usmba.cinema.modele.Film;
import ma.usmba.cinema.modele.GenreFilm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FilmDepot extends JpaRepository<Film, Long> {

    List<Film> findAllByOrderByTitreAsc();

    List<Film> findByGenreOrderByTitreAsc(GenreFilm genre);

    List<Film> findByTitreContainingIgnoreCaseOrderByTitreAsc(String terme);

    Optional<Film> findByTitreIgnoreCase(String titre);

    boolean existsByTitreIgnoreCaseAndRealisateurIgnoreCase(String titre, String realisateur);

    @Query("select f.genre, count(f) from Film f group by f.genre order by f.genre")
    List<Object[]> compterParGenre();

    @Query("select count(distinct f.realisateur) from Film f")
    long compterRealisateurs();
}
