package ma.usmba.cinema.configuration;

import ma.usmba.cinema.depot.FilmDepot;
import ma.usmba.cinema.modele.Film;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConvertisseurFilm implements Converter<String, Film> {

    private final FilmDepot filmDepot;

    public ConvertisseurFilm(FilmDepot filmDepot) {
        this.filmDepot = filmDepot;
    }

    @Override
    public Film convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return filmDepot.findById(Long.valueOf(source.trim())).orElse(null);
    }
}
