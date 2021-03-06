package ee.tuleva.epis.account;

import ee.tuleva.epis.config.UserPrincipal;
import ee.tuleva.epis.contact.ContactDetails;
import ee.tuleva.epis.contact.ContactDetails.Distribution;
import ee.tuleva.epis.contact.ContactDetailsService;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.converter.EpisX14TypeToCashFlowStatementConverter;
import ee.tuleva.epis.epis.converter.EpisX14TypeToFundBalancesConverter;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.tuleva.epis.fund.Fund;
import ee.tuleva.epis.fund.FundService;
import ee.x_road.epis.producer.EpisX14Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountStatementService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final ContactDetailsService contactDetailsService;
    private final EpisX14TypeToFundBalancesConverter toFundBalancesConverter;
    private final EpisX14TypeToCashFlowStatementConverter toCashFlowStatementConverter;
    private final FundService fundService;
    private final AccountStatementRequestFactory requestFactory;

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
            } else {
                log.error("Missing fund info for: " + fund.getIsin());
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
        EpisMessage episMessage = requestFactory.buildQuery(personalCode, startDate, endDate);
        episService.send(episMessage);
        return episMessage;
    }

}
