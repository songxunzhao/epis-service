package ee.tuleva.epis.account;

import ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory;
import ee.tuleva.epis.contact.ContactDetailsService;
import ee.tuleva.epis.epis.EpisMessageWrapper;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.converter.EpisX14TypeToCashFlowStatementConverter;
import ee.tuleva.epis.epis.converter.EpisX14TypeToFundBalancesConverter;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.tuleva.epis.fund.Fund;
import ee.tuleva.epis.fund.FundService;
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
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountStatementService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisMessageWrapper episMessageWrapper;
    private final ContactDetailsService contactDetailsService;
    private final EpisX14TypeToFundBalancesConverter toFundBalancesConverter;
    private final EpisX14TypeToCashFlowStatementConverter toCashFlowStatementConverter;
    private final EpisMessageFactory episMessageFactory;
    private final FundService fundService;

    List<FundBalance> get(String personalCode) {
        EpisMessage message = sendQuery(personalCode);
        EpisX14Type response = episMessageResponseStore.pop(message.getId(), EpisX14Type.class);

        List<FundBalance> fundBalances = toFundBalancesConverter.convert(response);
        fundBalances = resolveActiveFund(fundBalances, personalCode);
        fundBalances = resolveFundPillars(fundBalances);

        return fundBalances;
    }

    CashFlowStatement getTransactions(String personalCode, LocalDate startDate, LocalDate endDate) {
        EpisMessage message = sendQuery(personalCode, startDate, endDate);
        EpisX14Type response = episMessageResponseStore.pop(message.getId(), EpisX14Type.class);

        CashFlowStatement cashFlowStatement = toCashFlowStatementConverter.convert(response);
        return cashFlowStatement;
    }

    private List<FundBalance> resolveActiveFund(List<FundBalance> fundBalances, String personalCode) {
        String activeFundIsin = contactDetailsService.get(personalCode).getActiveSecondPillarFundIsin();

        boolean isActiveFundPresent = fundBalances.stream()
            .anyMatch(fundBalance -> fundBalance.getIsin().equalsIgnoreCase(activeFundIsin));

        if (isActiveFundPresent) {
            fundBalances.forEach(fundBalance -> {
                if (fundBalance.getIsin().equalsIgnoreCase(activeFundIsin)) {
                    fundBalance.setActiveContributions(true);
                }
            });
        } else {
            fundBalances.add(createActiveFundBalance(activeFundIsin));
        }

        return fundBalances;
    }

    private List<FundBalance> resolveFundPillars(List<FundBalance> fundBalances) {
        List<Fund> funds = fundService.getPensionFunds();
        Map<String, Integer> isinToPillar = funds.stream().collect(toMap(Fund::getIsin, Fund::getPillar));
        fundBalances.forEach(fund -> {
            if (isinToPillar.containsKey(fund.getIsin())) {
                fund.setPillar(isinToPillar.get(fund.getIsin()));
            }
        });
        return fundBalances;
    }

    private FundBalance createActiveFundBalance(String activeFundIsin) {
        return FundBalance.builder()
            .value(BigDecimal.ZERO)
            .currency("EUR")
            .activeContributions(true)
            .isin(activeFundIsin)
            .build();
    }

    private EpisMessage sendQuery(String personalCode) {
        return sendQuery(personalCode, null, null);
    }

    private EpisMessage sendQuery(String personalCode, LocalDate startDate, LocalDate endDate) {
        PersonDataRequestType personalData = episMessageFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX14RequestType request = episMessageFactory.createEpisX14RequestType();
        request.setPersonalData(personalData);

        if (startDate != null) {
            request.setStartDate(localDateToXMLGregorianCalendar(startDate));
        }
        if (endDate != null) {
            request.setEndDate(localDateToXMLGregorianCalendar(endDate));
        }

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

    private XMLGregorianCalendar localDateToXMLGregorianCalendar(LocalDate date) {
        try {
            GregorianCalendar gregorianCalendar = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
