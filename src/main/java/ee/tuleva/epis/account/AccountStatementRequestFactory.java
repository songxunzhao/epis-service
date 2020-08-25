package ee.tuleva.epis.account;

import ee.tuleva.epis.config.ObjectFactoryConfiguration;
import ee.tuleva.epis.epis.converter.LocalDateToXmlGregorianCalendarConverter;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.request.EpisMessageWrapper;
import ee.x_road.epis.producer.EpisX14RequestType;
import ee.x_road.epis.producer.EpisX14Type;
import ee.x_road.epis.producer.PersonDataRequestType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.time.Clock;
import java.time.LocalDate;

@RequiredArgsConstructor
@Service
class AccountStatementRequestFactory {

    private final EpisMessageWrapper episMessageWrapper;
    private final ObjectFactoryConfiguration.EpisMessageFactory episMessageFactory;
    private final LocalDateToXmlGregorianCalendarConverter dateConverter;
    private final Clock clock;

    EpisMessage buildQuery(String personalCode, LocalDate startDate, LocalDate endDate) {
        PersonDataRequestType personalData = episMessageFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX14RequestType request = episMessageFactory.createEpisX14RequestType();
        request.setPersonalData(personalData);

        if (startDate != null) {
            request.setStartDate(dateConverter.convert(startDate));
        } else {
            request.setStartDate(dateConverter.convert(LocalDate.of(2000, 1, 1)));
        }
        if (endDate != null) {
            request.setEndDate(dateConverter.convert(endDate));
        } else {
            request.setEndDate(dateConverter.convert(LocalDate.now(clock)));
        }

        EpisX14Type episX14Type = episMessageFactory.createEpisX14Type();
        episX14Type.setRequest(request);

        JAXBElement<EpisX14Type> accountStatementRequest = episMessageFactory.createKONTOVALJAVOTE(episX14Type);

        return episMessageWrapper.createWrappedMessage(accountStatementRequest);
    }
}
