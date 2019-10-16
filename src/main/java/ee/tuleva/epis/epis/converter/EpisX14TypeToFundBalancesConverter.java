package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.account.FundBalance;
import ee.tuleva.epis.epis.validator.EpisResultValidator;
import ee.x_road.epis.producer.EpisX14ResponseType.Unit;
import ee.x_road.epis.producer.EpisX14Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class EpisX14TypeToFundBalancesConverter implements Converter<EpisX14Type, List<FundBalance>> {

    private final EpisResultValidator resultValidator;

    @Override
    @NonNull
    public List<FundBalance> convert(EpisX14Type source) {
        log.info("Converting EpisX14Type to Fund Balance List");
        resultValidator.validate(source.getResponse().getResults());

        List<FundBalance> fundBalances = new ArrayList<>(
            source.getResponse().getUnit().stream()
            .filter(this::isEnd)
            .map((Unit unit) ->
                FundBalance.builder()
                    .currency(unit.getCurrency())
                    .isin(unit.getISIN())
                    .value(isRegular(unit) ? calculateValue(unit) : ZERO)
                    .unavailableValue(isBron(unit) ? calculateValue(unit) : ZERO)
                    .units(isRegular(unit) ? unit.getAmount() : ZERO)
                    .unavailableUnits(isBron(unit) ? unit.getAmount() : ZERO)
                    .nav(unit.getNAV())
                    .build())

            // Response might have duplicate elements
            .collect(
                toMap(FundBalance::getIsin, fundBalance -> fundBalance, (fundBalance1, fundBalance2) ->
                    FundBalance.builder()
                        .currency(fundBalance1.getCurrency())
                        .isin(fundBalance1.getIsin())
                        .value(fundBalance1.getValue().add(fundBalance2.getValue()))
                        .unavailableValue(fundBalance1.getUnavailableValue().add(fundBalance2.getUnavailableValue()))
                        .units(fundBalance1.getUnits().add(fundBalance2.getUnits()))
                        .unavailableUnits(fundBalance1.getUnavailableUnits().add(fundBalance2.getUnavailableUnits()))
                        .nav(fundBalance1.getNav())
                        .build()))
            .values()
        );

        log.info("Fund balances converted: {}", fundBalances);
        return fundBalances;

    }

    private boolean isEnd(Unit unit) {
        return "END".equals(unit.getCode());
    }

    private boolean isRegular(Unit unit) {
        return unit.getAdditionalFeature() == null;
    }

    private boolean isBron(Unit unit) {
        return "BRON".equals(unit.getAdditionalFeature());
    }

    private BigDecimal calculateValue(Unit unit) {
        return unit.getAmount().multiply(unit.getNAV()).setScale(2, HALF_UP);
    }

}
