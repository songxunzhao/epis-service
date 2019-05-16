package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.account.CashFlowStatement;
import ee.tuleva.epis.account.Transaction;
import ee.tuleva.epis.epis.exception.EpisMessageException;
import ee.x_road.epis.producer.AnswerType;
import ee.x_road.epis.producer.EpisX14ResponseType.Cash;
import ee.x_road.epis.producer.EpisX14ResponseType.Unit;
import ee.x_road.epis.producer.EpisX14Type;
import ee.x_road.epis.producer.ResultType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
@Slf4j
public class EpisX14TypeToCashFlowStatementConverter implements Converter<EpisX14Type, CashFlowStatement> {

    private static final Map<String, Integer> CODE_TO_PILLAR = new HashMap<String, Integer>() {{
        put("RIF", 2); // Rahad Investorilt Fondile
        put("MIF", 3); // Rahad Investorilt Fondile (3. sammas)
    }};

    @Override
    @NonNull
    public CashFlowStatement convert(EpisX14Type source) {
        log.info("Converting EpisX14Type to CashFlowSatement");
        validateResult(source.getResponse().getResults());

        CashFlowStatement cashFlowStatement = new CashFlowStatement();

        for (Unit unit : source.getResponse().getUnit()) {
            if ("BRON".equals(unit.getAdditionalFeature())) {
                log.info("Ignoring BRON unit.");
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

    private void validateResult(ResultType result) {
        if (result.getResult().equals(AnswerType.NOK)) {
            throw new EpisMessageException("Got error code " + result.getResultCode() + " from EPIS: "
                + result.getErrorTextEng());
        }
    }

    private Transaction unitToTransaction(Unit unit) {
        return Transaction.builder()
            .time(unit.getTransactionDate().toGregorianCalendar().toInstant())
            .amount(unit.getNAV().multiply(unit.getAmount()))
            .currency(unit.getCurrency())
            .replaceNulls()
            .build();
    }

    private boolean isCashInStatement(Cash cash) {
        return CODE_TO_PILLAR.keySet().contains(cash.getCode());
    }

    private Transaction cashToTransaction(Cash cash) {
        return Transaction.builder()
            .time(cash.getTransactionDate().toGregorianCalendar().toInstant())
            .amount(cash.getAmount())
            .currency(cash.getCurrency())
            .pillar(CODE_TO_PILLAR.get(cash.getCode()))
            .replaceNulls()
            .build();
    }

}
