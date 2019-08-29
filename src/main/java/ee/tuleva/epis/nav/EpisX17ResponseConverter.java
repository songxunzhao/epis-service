package ee.tuleva.epis.nav;

import ee.x_road.epis.producer.EpisX17ResponseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static ee.x_road.epis.producer.AnswerType.OK;

@Service
@RequiredArgsConstructor
class EpisX17ResponseConverter {
    Optional<NavData> convert(EpisX17ResponseType response) {
        if (response.getResults().getResult() != OK) return Optional.empty();
        return Optional.of(
            new NavData(
                response.getISIN(),
                convertToEstonianLocalDate(response.getValidityDate()),
                response.getNAV()));
    }

    private LocalDate convertToEstonianLocalDate(XMLGregorianCalendar date) {
        return date.toGregorianCalendar().toInstant().atZone(ZoneId.of("Europe/Tallinn")).toLocalDate();
    }
}
