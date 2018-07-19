package ee.tuleva.epis.account;

import ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory;
import ee.tuleva.epis.contact.ContactDetailsService;
import ee.tuleva.epis.epis.EpisMessageWrapper;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.converter.EpisX14TypeToFundBalanceListConverter;
import ee.tuleva.epis.epis.converter.EpisX14TypeToCashFlowStatementConverter;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.x_road.epis.producer.EpisX14RequestType;
import ee.x_road.epis.producer.EpisX14Type;
import ee.x_road.epis.producer.PersonDataRequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountStatementService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisMessageWrapper episMessageWrapper;
    private final ContactDetailsService contactDetailsService;
    private final EpisX14TypeToFundBalanceListConverter converter;
    private final EpisX14TypeToCashFlowStatementConverter toCashFlowStatementConverter;
    private final EpisMessageFactory episMessageFactory;

    List<FundBalance> get(String personalCode) {
        EpisMessage message = sendQuery(personalCode);
        EpisX14Type response = episMessageResponseStore.pop(message.getId(), EpisX14Type.class);

        return resolveActiveFund(converter.convert(response), personalCode);
    }

    public CashFlowStatement getTransactions(String personalCode, LocalDate startDate, LocalDate endDate) throws DatatypeConfigurationException {
        EpisMessage message = sendQuery(personalCode, startDate, endDate);
        EpisX14Type response = episMessageResponseStore.pop(message.getId(), EpisX14Type.class);

        return toCashFlowStatementConverter.convert(response);
    }

    private List<FundBalance> resolveActiveFund(List<FundBalance> fundBalanceList, String personalCode) {
        String activeFundIsin = contactDetailsService.get(personalCode).getActiveSecondPillarFundIsin();

        boolean isActiveFundPresent = fundBalanceList.stream()
                .anyMatch(fundBalance -> fundBalance.getIsin().equalsIgnoreCase(activeFundIsin));

        if(isActiveFundPresent) {
            return fundBalanceList.stream().map(fundBalance -> {
                if(fundBalance.getIsin().equalsIgnoreCase(activeFundIsin)) {
                    fundBalance.setActiveContributions(true);
                }
                return fundBalance;
            }).collect(Collectors.toList());
        } else {
            fundBalanceList.add(createActiveFundBalance(activeFundIsin));
            return fundBalanceList;
        }
    }

    private FundBalance createActiveFundBalance(String activeFundIsin) {
        return FundBalance.builder()
                .value(BigDecimal.ZERO)
                .currency("EUR")
                .pillar(2)
                .activeContributions(true)
                .isin(activeFundIsin)
                .build();
    }

    private XMLGregorianCalendar localDateToXMLGregorianCalendar(LocalDate date) throws DatatypeConfigurationException {
        GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
    }

    private EpisMessage sendQuery(String personalCode, LocalDate startDate, LocalDate endDate) throws DatatypeConfigurationException {
        PersonDataRequestType personalData = episMessageFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX14RequestType request = episMessageFactory.createEpisX14RequestType();
        request.setPersonalData(personalData);

        request.setStartDate(localDateToXMLGregorianCalendar(startDate));
        request.setEndDate(localDateToXMLGregorianCalendar(endDate));

        EpisX14Type episX14Type = episMessageFactory.createEpisX14Type();
        episX14Type.setRequest(request);

        JAXBElement<EpisX14Type> personalDataRequest = episMessageFactory.createKONTOVALJAVOTE(episX14Type);

        String id = UUID.randomUUID().toString().replace("-", "");
        Ex ex = episMessageWrapper.wrap(id, personalDataRequest);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());
        return episMessage;
    }

    private EpisMessage sendQuery(String personalCode) {
        PersonDataRequestType personalData = episMessageFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX14RequestType request = episMessageFactory.createEpisX14RequestType();
        request.setPersonalData(personalData);

        EpisX14Type episX14Type = episMessageFactory.createEpisX14Type();
        episX14Type.setRequest(request);

        JAXBElement<EpisX14Type> personalDataRequest = episMessageFactory.createKONTOVALJAVOTE(episX14Type);

        String id = UUID.randomUUID().toString().replace("-", "");
        Ex ex = episMessageWrapper.wrap(id, personalDataRequest);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());
        return episMessage;
    }

}
