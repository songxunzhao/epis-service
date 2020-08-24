package ee.tuleva.epis.nav;

import ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory;
import ee.tuleva.epis.epis.converter.LocalDateToXmlGregorianCalendarConverter;
import ee.x_road.epis.producer.EpisX17RequestType;
import ee.x_road.epis.producer.EpisX17Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
class EpisX17RequestFactory {
    private final EpisMessageFactory episMessageFactory;
    private final LocalDateToXmlGregorianCalendarConverter dateConverter;

    JAXBElement<EpisX17Type> create(String isin, LocalDate date) {
        EpisX17RequestType x17Request = episMessageFactory.createEpisX17RequestType();
        x17Request.setISIN(isin);
        x17Request.setValidityDate(dateConverter.convert(date));

        EpisX17Type x17 = episMessageFactory.createEpisX17Type();
        x17.setRequest(x17Request);

        return episMessageFactory.createNAVVAARTUSEPARING(x17);
    }
}
