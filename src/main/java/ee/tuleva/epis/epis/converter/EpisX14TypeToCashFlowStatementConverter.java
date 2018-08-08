package ee.tuleva.epis.epis.converter;

import com.google.common.collect.ImmutableSet;
import ee.tuleva.epis.account.CashFlowStatement;
import ee.tuleva.epis.account.Transaction;
import ee.tuleva.epis.epis.exception.EpisMessageException;
import ee.x_road.epis.producer.AnswerType;
import ee.x_road.epis.producer.EpisX14ResponseType;
import ee.x_road.epis.producer.EpisX14Type;
import ee.x_road.epis.producer.ResultType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.stream.Collectors.toList;

import static ee.tuleva.epis.epis.converter.EpisX14TypeToFundBalanceListConverter.IGNORED_3RD_PILLAR_FUND_ISINS;

@Component
@Slf4j
public class EpisX14TypeToCashFlowStatementConverter implements Converter<EpisX14Type, CashFlowStatement> {

    private static final Set<String> BALANCE_CODES = ImmutableSet.of("BEGIN", "END");

    private boolean isBalanceStatement(EpisX14ResponseType.Unit unit) {
        return BALANCE_CODES.contains(unit.getCode());
    }
    private boolean isCashInStatement(EpisX14ResponseType.Cash cash) { return "RIF".equals(cash.getCode()); }

    @Override
    public CashFlowStatement convert(EpisX14Type source) {
        log.info("Converting EpisX14Type to CashFlowSatement");
        validateResult(source.getResponse().getResults());

        CashFlowStatement cashFlowStatement = new CashFlowStatement();

        for (EpisX14ResponseType.Unit unit: source.getResponse().getUnit()) {
            if ("BRON".equals(unit.getAdditionalFeature())) {
                log.info("Ignoring BRON unit.");
                continue;
            }
            if (IGNORED_3RD_PILLAR_FUND_ISINS.stream().anyMatch(isin -> isin.equalsIgnoreCase(unit.getISIN()))) {
                continue;
            }
            if ("BEGIN".equals(unit.getCode())) {
                cashFlowStatement.putStartBalance(unit.getISIN(), unitToTransaction(unit));
            } else if ("END".equals(unit.getCode())) {
                cashFlowStatement.putEndBalance(unit.getISIN(), unitToTransaction(unit));
            }
        }

        cashFlowStatement.setTransactions(
            source.getResponse().getCash().stream()
                .filter(this::isCashInStatement)
                .map(this::cashToTransaction)
                .collect(toList())
        );

        log.info("CashFlowStatement created.");
        return cashFlowStatement;

    }

    private Transaction cashToTransaction(EpisX14ResponseType.Cash cash) {
        return Transaction.builder()
            .time(cash.getTransactionDate().toGregorianCalendar().toInstant())
            .amount(cash.getAmount())
            .currency(cash.getCurrency())
            .build()
            .replaceNulls();
    }

    private Transaction unitToTransaction(EpisX14ResponseType.Unit unit) {
        return Transaction.builder()
            .time(unit.getTransactionDate().toGregorianCalendar().toInstant())
            .amount(unit.getNAV().multiply(unit.getAmount()))
            .currency(unit.getCurrency())
            .build()
            .replaceNulls();
    }

    private void validateResult(ResultType result) {
        if (result.getResult().equals(AnswerType.NOK)) {
            throw new EpisMessageException("Got error code " + result.getResultCode() + " from EPIS: "
                    + result.getErrorTextEng());
        }
    }

}
