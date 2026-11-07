package ma.usmba.cinema.configuration;

import ma.usmba.cinema.depot.SeanceDepot;
import ma.usmba.cinema.modele.Seance;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConvertisseurSeance implements Converter<String, Seance> {

    private final SeanceDepot seanceDepot;

    public ConvertisseurSeance(SeanceDepot seanceDepot) {
        this.seanceDepot = seanceDepot;
    }

    @Override
    public Seance convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return seanceDepot.findById(Long.valueOf(source.trim())).orElse(null);
    }
}
