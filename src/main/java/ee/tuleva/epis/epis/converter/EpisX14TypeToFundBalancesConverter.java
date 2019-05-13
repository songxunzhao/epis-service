package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.account.FundBalance;
import ee.tuleva.epis.epis.exception.EpisMessageException;
import ee.x_road.epis.producer.AnswerType;
import ee.x_road.epis.producer.EpisX14ResponseType.Unit;
import ee.x_road.epis.producer.EpisX14Type;
import ee.x_road.epis.producer.ResultType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
public class EpisX14TypeToFundBalancesConverter implements Converter<EpisX14Type, List<FundBalance>> {

    @Override
    public List<FundBalance> convert(EpisX14Type source) {
        log.info("Converting EpisX14Type to Fund Balance List");
        validateResult(source.getResponse().getResults());

        List<FundBalance> fundBalances = source.getResponse().getUnit().stream()
                .filter(unit -> "END".equals(unit.getCode()))
                .map((Unit unit) ->
                FundBalance.builder()
                .currency(unit.getCurrency())
                .isin(unit.getISIN())
                .value(multiply(unit.getAmount(), unit.getNAV()))
                .build())

                // Response might have duplicate elements
                .collect(toMap(FundBalance::getIsin, p -> p, (p, q) -> p))
                .entrySet().stream().map(Map.Entry::getValue)

                .collect(toList());

        log.info("Fund balances converted. Size: {}", fundBalances.size());
        return fundBalances;

    }

    private void validateResult(ResultType result) {
        if (result.getResult().equals(AnswerType.NOK)) { // OK
            throw new EpisMessageException("Got error code " + result.getResultCode() + " from EPIS: "
                    + result.getErrorTextEng());
        }
    }

    private BigDecimal multiply(BigDecimal multiplicand, BigDecimal multiplier) {
        multiplicand = multiplicand == null ? ZERO : multiplicand;
        multiplier = multiplier == null ? ZERO : multiplier;
        return multiplicand.multiply(multiplier);
    }

}
