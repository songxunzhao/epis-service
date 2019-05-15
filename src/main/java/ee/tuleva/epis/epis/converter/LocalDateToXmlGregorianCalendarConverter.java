package ee.tuleva.epis.epis.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;

@Component
@Slf4j
public class LocalDateToXmlGregorianCalendarConverter implements Converter<LocalDate, XMLGregorianCalendar> {

    @Override
    public XMLGregorianCalendar convert(LocalDate localDate) {
        try {
            GregorianCalendar gregorianCalendar =
                GregorianCalendar.from(localDate.atStartOfDay(ZoneId.systemDefault()));
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
