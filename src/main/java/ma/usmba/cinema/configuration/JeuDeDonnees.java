package ma.usmba.cinema.configuration;

import ma.usmba.cinema.depot.BilletDepot;
import ma.usmba.cinema.depot.FilmDepot;
import ma.usmba.cinema.depot.SeanceDepot;
import ma.usmba.cinema.modele.Billet;
import ma.usmba.cinema.modele.Film;
import ma.usmba.cinema.modele.GenreFilm;
import ma.usmba.cinema.modele.Seance;
import ma.usmba.cinema.modele.StatutBillet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class JeuDeDonnees implements CommandLineRunner {

    private static final Logger JOURNAL = LoggerFactory.getLogger(JeuDeDonnees.class);

    private static final String[] PRENOMS = {
            "Ahmed", "Salma", "Yassine", "Karim", "Nadia", "Omar", "Khadija", "Hamza",
            "Soukaina", "Rachid", "Mehdi", "Amina", "Leila", "Hind", "Réda", "Nawal",
            "Said", "Imane", "Anas", "Widad", "Sara", "Amine", "Loubna", "Zineb",
            "Ilham", "Hicham", "Fatima", "Youssef", "Meryem", "Abdellah"};

    private static final String[] NOMS = {
            "Benidar", "Idrissi", "Ouali", "Sbai", "Fassi", "Belkacem", "Amrani", "Rifai",
            "Berrich", "Chraibi", "Bouzid", "Benslimane", "Naciri", "Himmi", "Berrada",
            "Tazi", "El Alaoui", "Kabbaj", "Lahlou", "Sekkat", "Cherkaoui", "Kettani",
            "Bouziane", "Zerhouni", "Boukhris", "Ait Lhaj", "El Ouazzani", "Sqalli", "Bennani"};

    private final FilmDepot filmDepot;
    private final SeanceDepot seanceDepot;
    private final BilletDepot billetDepot;

    public JeuDeDonnees(FilmDepot filmDepot, SeanceDepot seanceDepot, BilletDepot billetDepot) {
        this.filmDepot = filmDepot;
        this.seanceDepot = seanceDepot;
        this.billetDepot = billetDepot;
    }

    @Override
    public void run(String... args) {
        if (filmDepot.count() > 0) {
            JOURNAL.info("CineAtlas : catalogue déjà initialisé ({} films).", filmDepot.count());
            return;
        }

        Film desert = filmDepot.save(new Film("Les Lanternes du Draa", GenreFilm.DRAME, 132,
                "Nawal El Ouazzani", 2023));
        Film atelier = filmDepot.save(new Film("Atelier 7 : la dernière coupe", GenreFilm.ACTION, 118,
                "Youssef Berrada", 2024));
        Film medina = filmDepot.save(new Film("MEDINA 2049", GenreFilm.SCIENCE_FICTION, 145,
                "Salma Boukhris", 2022));
        Film resine = filmDepot.save(new Film("Résine et Safran", GenreFilm.COMEDIE, 96,
                "Hicham Ait Lhaj", 2021));
        Film craie = filmDepot.save(new Film("La craie du tableau noir", GenreFilm.DOCUMENTAIRE, 74,
                "Imane Cherkaoui", 2020));
        Film cedre = filmDepot.save(new Film("Le Cèdre bleu", GenreFilm.ANIMATION, 88,
                "Karim Zerhouni", 2024));

        LocalDateTime base = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        Seance s1 = seanceDepot.save(new Seance(base.plusDays(1).withHour(18), "SALLE ATLAS", "VF", 40, medina));
        Seance s2 = seanceDepot.save(new Seance(base.plusDays(1).withHour(21), "SALLE ATLAS", "VOSTFR", 40, desert));
        Seance s3 = seanceDepot.save(new Seance(base.plusDays(2).withHour(16), "SALLE OUARZAZATE", "VF", 30, cedre));
        Seance s4 = seanceDepot.save(new Seance(base.plusDays(2).withHour(19), "SALLE OUARZAZATE", "VF", 30, atelier));
        Seance s5 = seanceDepot.save(new Seance(base.plusDays(3).withHour(17), "SALLE TETOUAN", "VOSTFR", 20, craie));
        Seance s6 = seanceDepot.save(new Seance(base.plusDays(3).withHour(20), "SALLE TETOUAN", "VF", 20, resine));
        Seance s7 = seanceDepot.save(new Seance(base.plusDays(4).withHour(15), "SALLE ATLAS", "VF", 20, cedre));
        Seance s8 = seanceDepot.save(new Seance(base.minusDays(2).withHour(20), "SALLE OUARZAZATE", "VF", 40, desert));

        List<Billet> billets = new ArrayList<>();
        int rang = 0;
        rang = remplir(billets, rang, s8, new BigDecimal("60.00"), 3, 9, 0, base.minusDays(2).withHour(18));
        rang = remplir(billets, rang, s1, new BigDecimal("55.00"), 3, 0, 11, base.minusHours(9));
        rang = remplir(billets, rang, s2, new BigDecimal("70.00"), 2, 0, 9, base.minusHours(7));
        rang = remplir(billets, rang, s3, new BigDecimal("40.00"), 1, 0, 9, base.minusHours(5));
        rang = remplir(billets, rang, s4, new BigDecimal("50.00"), 1, 0, 11, base.minusHours(4));
        rang = remplir(billets, rang, s5, new BigDecimal("35.00"), 1, 0, 7, base.minusHours(3));
        rang = remplir(billets, rang, s6, new BigDecimal("45.00"), 1, 0, 13, base.minusHours(2));
        remplir(billets, rang, s7, new BigDecimal("40.00"), 0, 0, 20, base.minusMinutes(90));

        billetDepot.saveAll(billets);

        JOURNAL.info("CineAtlas : {} films, {} séances et {} billets insérés.",
                filmDepot.count(), seanceDepot.count(), billetDepot.count());
    }

    private int remplir(List<Billet> billets, int depart, Seance seance, BigDecimal prix,
                        int annules, int utilises, int valides, LocalDateTime fin) {
        int total = annules + utilises + valides;
        for (int i = 0; i < total; i++) {
            StatutBillet statut = i < annules ? StatutBillet.ANNULE
                    : i < annules + utilises ? StatutBillet.UTILISE
                    : StatutBillet.VALIDE;
            LocalDateTime achat = fin.minusMinutes((long) (total - i) * 83);
            billets.add(billet(depart + i, seance, prix, statut, achat));
        }
        return depart + total;
    }

    private Billet billet(int rang, Seance seance, BigDecimal prix, StatutBillet statut, LocalDateTime achat) {
        String acheteur = PRENOMS[rang % PRENOMS.length] + " " + NOMS[(rang * 7 + rang / PRENOMS.length) % NOMS.length];
        String telephone = String.format("06%08d", 10000000 + (rang * 137L) % 89999999L);
        return new Billet(prix, statut, achat, acheteur, telephone, seance);
    }
}
