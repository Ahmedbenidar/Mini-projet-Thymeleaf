package ma.usmba.cinema.depot;

import ma.usmba.cinema.modele.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SeanceDepot extends JpaRepository<Seance, Long> {

    List<Seance> findAllByOrderByDateHeureAsc();

    List<Seance> findBySalleIgnoreCaseOrderByDateHeureAsc(String salle);

    List<Seance> findByDateHeureBetweenOrderByDateHeureAsc(LocalDateTime debut, LocalDateTime fin);

    List<Seance> findByFilmIdOrderByDateHeureAsc(Long filmId);

    long countByFilmId(Long filmId);

    @Query("select s from Seance s where s.dateHeure >= :maintenant order by s.dateHeure asc")
    List<Seance> trouverAVenir(@Param("maintenant") LocalDateTime maintenant);

    @Query("select distinct s.salle from Seance s order by s.salle")
    List<String> listerSalles();

    @Query("select coalesce(sum(s.capacite), 0) from Seance s")
    long totalPlacesOffertes();
}
