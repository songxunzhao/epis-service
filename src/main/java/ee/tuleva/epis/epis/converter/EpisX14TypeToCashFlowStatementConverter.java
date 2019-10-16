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

import static java.math.RoundingMode.HALF_UP;

@Component
@Slf4j
@RequiredArgsConstructor
public class EpisX14TypeToCashFlowStatementConverter implements Converter<EpisX14Type, CashFlowStatement> {

    private final EpisResultValidator resultValidator;

    @Override
    @NonNull
    public CashFlowStatement convert(EpisX14Type source) {
        log.info("Converting EpisX14Type to CashFlowStatement");
        resultValidator.validate(source.getResponse().getResults());

        CashFlowStatement cashFlowStatement = new CashFlowStatement();

        for (Unit unit : source.getResponse().getUnit()) {
            if (isBron(unit) && !isEnd(unit)) {
                log.info("Ignoring non-END BRON unit.");
                continue;
            }
//            if (isUfr(unit)) {
//                log.info("Ignoring UFR bron unit.");
//                continue;
//            }
            if (unit.getAmount() == null || unit.getCurrency() == null) {
                log.info("Ignoring unit with null values: " + unit);
                continue;
            }
            if (isBegin(unit)) {
                cashFlowStatement.addStartBalance(unit.getISIN(), unitToTransaction(unit));
            } else if (isEnd(unit)) {
                cashFlowStatement.addEndBalance(unit.getISIN(), unitToTransaction(unit));
            } else {
                cashFlowStatement.addTransaction(unitToTransaction(unit));
            }
        }

        log.info("CashFlowStatement created.");
        return cashFlowStatement;

    }

    private boolean isBron(Unit unit) {
        return "BRON".equals(unit.getAdditionalFeature());
    }

    private boolean isUfr(Unit unit) {
        return "UFR".equals(unit.getCode());
    }

    private boolean isBegin(Unit unit) {
        return "BEGIN".equals(unit.getCode());
    }

    private boolean isEnd(Unit unit) {
        return "END".equals(unit.getCode());
    }

    private Transaction unitToTransaction(Unit unit) {
        return Transaction.builder()
            .isin(unit.getISIN())
            .date(unit.getTransactionDate().toGregorianCalendar().toZonedDateTime().toLocalDate())
            .units(unit.getAmount())
            .amount(getAmount(unit))
            .build();
    }

    private BigDecimal getAmount(Unit unit) {
        BigDecimal price = unit.getPrice() != null ? unit.getPrice() : unit.getNAV();

        if (price == null) {
            log.error("Unknown price in transaction: " + unit);
            return BigDecimal.ZERO;
        }

        BigDecimal amount = price.multiply(unit.getAmount());

        if ("EEK".equals(unit.getCurrency())) {
            return amount.divide(new BigDecimal("15.6466"), 2, HALF_UP);
        } else if ("EUR".equals(unit.getCurrency())) {
            return amount.setScale(2, HALF_UP);
        } else {
            throw new IllegalArgumentException("Unknown currency in transaction: " + unit.getCurrency());
        }
    }

}
