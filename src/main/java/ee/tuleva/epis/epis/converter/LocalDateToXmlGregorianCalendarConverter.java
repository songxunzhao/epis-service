package ee.tuleva.epis.epis.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;

@Component
@Slf4j
public class LocalDateToXmlGregorianCalendarConverter implements Converter<LocalDate, XMLGregorianCalendar> {

    @Override
    @NonNull
    public XMLGregorianCalendar convert(LocalDate localDate) {
        try {
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            calendar.setYear(localDate.getYear());
            calendar.setMonth(localDate.getMonthValue());
            calendar.setDay(localDate.getDayOfMonth());
            return calendar;
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
