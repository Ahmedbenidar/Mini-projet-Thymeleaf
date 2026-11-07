package ma.usmba.cinema.depot;

import ma.usmba.cinema.modele.Billet;
import ma.usmba.cinema.modele.StatutBillet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BilletDepot extends JpaRepository<Billet, Long> {

    List<Billet> findAllByOrderByDateAchatDesc();

    List<Billet> findByStatutOrderByDateAchatDesc(StatutBillet statut);

    List<Billet> findBySeanceIdOrderByDateAchatDesc(Long seanceId);

    List<Billet> findByAcheteurContainingIgnoreCaseOrTelephoneContainingOrderByDateAchatDesc(String acheteur, String telephone);

    long countBySeanceIdAndStatutNot(Long seanceId, StatutBillet statut);

    @Query("select b.seance.id, count(b) from Billet b where b.statut <> ma.usmba.cinema.modele.StatutBillet.ANNULE group by b.seance.id")
    List<Object[]> compterParSeance();

    @Query("select coalesce(sum(b.prix), 0) from Billet b where b.statut <> :annule")
    java.math.BigDecimal chiffreAffairesTotal(@Param("annule") StatutBillet annule);

    @Query("select b.seance.film.titre, count(b), coalesce(sum(b.prix), 0) "
            + "from Billet b where b.statut <> :annule group by b.seance.film.titre order by count(b) desc")
    List<Object[]> recettesParFilm(@Param("annule") StatutBillet annule);

    @Query("select function('date_format', b.dateAchat, '%Y-%m'), count(b), coalesce(sum(b.prix), 0) "
            + "from Billet b where b.statut <> :annule group by function('date_format', b.dateAchat, '%Y-%m') "
            + "order by function('date_format', b.dateAchat, '%Y-%m')")
    List<Object[]> ventesParMois(@Param("annule") StatutBillet annule);

    long countByStatut(StatutBillet statut);

    @Query("select count(b) from Billet b where b.seance.dateHeure < :maintenant and b.statut = ma.usmba.cinema.modele.StatutBillet.VALIDE")
    long compterNonUtilisesApresSeance(@Param("maintenant") LocalDateTime maintenant);
}
