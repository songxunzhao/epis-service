package ee.tuleva.epis.account;

import ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory;
import ee.tuleva.epis.config.UserPrincipal;
import ee.tuleva.epis.contact.ContactDetails;
import ee.tuleva.epis.contact.ContactDetails.Distribution;
import ee.tuleva.epis.contact.ContactDetailsService;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.converter.EpisX14TypeToCashFlowStatementConverter;
import ee.tuleva.epis.epis.converter.EpisX14TypeToFundBalancesConverter;
import ee.tuleva.epis.epis.converter.LocalDateToXmlGregorianCalendarConverter;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.request.EpisMessageWrapper;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;
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
    private final LocalDateToXmlGregorianCalendarConverter dateConverter;

    public List<FundBalance> getAccountStatement(UserPrincipal principal) {
        EpisMessage message = sendQuery(principal.getPersonalCode());
        EpisX14Type response = episMessageResponseStore.pop(message.getId(), EpisX14Type.class);

        List<FundBalance> fundBalances = toFundBalancesConverter.convert(response);
        resolveActiveFunds(fundBalances, principal);
        resolveFundPillars(fundBalances);

        return fundBalances;
    }

    private void resolveActiveFunds(List<FundBalance> fundBalances, UserPrincipal principal) {
        ContactDetails contactDetails = contactDetailsService.getContactDetails(principal);

        String active2ndPillarFundIsin = contactDetails.getActiveSecondPillarFundIsin();
        if (active2ndPillarFundIsin != null) {
            resolveActiveFund(fundBalances, active2ndPillarFundIsin);
        }

        List<Distribution> distributions = contactDetails.getThirdPillarDistribution();
        if (distributions != null) {
            distributions.forEach(distribution ->
                resolveActiveFund(fundBalances, distribution.getActiveThirdPillarFundIsin()));
        }
    }

    public CashFlowStatement getCashFlowStatement(String personalCode, LocalDate startDate, LocalDate endDate) {
        EpisMessage message = sendQuery(personalCode, startDate, endDate);
        EpisX14Type response = episMessageResponseStore.pop(message.getId(), EpisX14Type.class);
        return toCashFlowStatementConverter.convert(response);
    }

    private void resolveActiveFund(List<FundBalance> fundBalances, String activeFundIsin) {
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
    }

    private void resolveFundPillars(List<FundBalance> fundBalances) {
        Map<String, Integer> isinToPillar = getIsinToPillar();

        fundBalances.forEach(fund -> {
            if (isinToPillar.containsKey(fund.getIsin())) {
                fund.setPillar(isinToPillar.get(fund.getIsin()));
            }
        });
    }

    private Map<String, Integer> getIsinToPillar() {
        List<Fund> funds = fundService.getPensionFunds();
        return funds.stream().collect(toMap(Fund::getIsin, Fund::getPillar));
    }

    private FundBalance createActiveFundBalance(String activeFundIsin) {
        return FundBalance.builder()
            .value(ZERO)
            .currency("EUR")
            .activeContributions(true)
            .isin(activeFundIsin)
            .units(ZERO)
            .unavailableUnits(ZERO)
            .nav(null)
            .build();
    }

    private EpisMessage sendQuery(String personalCode) {
        return sendQuery(personalCode, null, null);
    }

    private EpisMessage sendQuery(String personalCode, LocalDate startDate, LocalDate endDate) {
        EpisMessage episMessage = buildQuery(personalCode, startDate, endDate);
        episService.send(episMessage);
        return episMessage;
    }

    private EpisMessage buildQuery(String personalCode, LocalDate startDate, LocalDate endDate) {
        PersonDataRequestType personalData = episMessageFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX14RequestType request = episMessageFactory.createEpisX14RequestType();
        request.setPersonalData(personalData);

        if (startDate != null) {
            request.setStartDate(dateConverter.convert(startDate));
        }
        if (endDate != null) {
            request.setEndDate(dateConverter.convert(endDate));
        }

        EpisX14Type episX14Type = episMessageFactory.createEpisX14Type();
        episX14Type.setRequest(request);

        JAXBElement<EpisX14Type> personalDataRequest = episMessageFactory.createKONTOVALJAVOTE(episX14Type);

        String id = UUID.randomUUID().toString().replace("-", "");
        Ex ex = episMessageWrapper.wrap(id, personalDataRequest);

        return EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();
    }
}
