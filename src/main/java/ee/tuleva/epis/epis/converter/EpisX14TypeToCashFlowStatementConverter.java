package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.account.CashFlowStatement;
import ee.tuleva.epis.account.Transaction;
import ee.tuleva.epis.epis.validator.EpisResultValidator;
import ee.x_road.epis.producer.EpisX14ResponseType.Unit;
import ee.x_road.epis.producer.EpisX14Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Slf4j
@RequiredArgsConstructor
public class EpisX14TypeToCashFlowStatementConverter implements Converter<EpisX14Type, CashFlowStatement> {

    private final EpisResultValidator resultValidator;

    @Override
    @NonNull
    public CashFlowStatement convert(EpisX14Type source) {
        log.info("Converting EpisX14Type to CashFlowSatement");
        resultValidator.validate(source.getResponse().getResults());

        CashFlowStatement cashFlowStatement = new CashFlowStatement();

        for (Unit unit : source.getResponse().getUnit()) {
            if ("BRON".equals(unit.getAdditionalFeature())) {
                log.info("Ignoring BRON unit.");
                continue;
            }
            if (unit.getAmount() == null || unit.getCurrency() == null) {
                log.info("Ignoring unit with null values: " + unit);
                continue;
            }
            if ("BEGIN".equals(unit.getCode())) {
                cashFlowStatement.putStartBalance(unit.getISIN(), unitToTransaction(unit));
            }
            else if ("END".equals(unit.getCode())) {
                cashFlowStatement.putEndBalance(unit.getISIN(), unitToTransaction(unit));
            }
            else {
                cashFlowStatement.getTransactions().add(unitToTransaction(unit));
            }
        }

        log.info("CashFlowStatement created.");
        return cashFlowStatement;

    }

    private Transaction unitToTransaction(Unit unit) {
        return Transaction.builder()
            .isin(unit.getISIN())
            .date(unit.getTransactionDate().toGregorianCalendar().toZonedDateTime().toLocalDate())
            .amount(getAmount(unit))
            .build();
    }

    private BigDecimal getAmount(Unit unit) {
        BigDecimal price = unit.getPrice() != null ? unit.getPrice() : unit.getNAV();
        BigDecimal amount = price.multiply(unit.getAmount());
        if ("EEK".equals(unit.getCurrency())) {
            amount = amount.divide(new BigDecimal("15.6466"), 2, RoundingMode.HALF_UP);
        }
        return amount;
    }

}
