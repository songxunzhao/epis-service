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

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
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

        List<FundBalance> fundBalances = source.getResponse().getUnit().stream()
            .filter(unit -> !"BRON".equals(unit.getAdditionalFeature()))
            .filter(unit -> "END".equals(unit.getCode()))
            .map((Unit unit) ->
                FundBalance.builder()
                    .currency(unit.getCurrency())
                    .isin(unit.getISIN())
                    .value(unit.getAmount().multiply(unit.getNAV()))
                    .units(unit.getAmount())
                    .nav(unit.getNAV())
                    .build())

            // Response might have duplicate elements
            .collect(toMap(FundBalance::getIsin, p -> p, (p, q) -> p))
            .entrySet().stream().map(Map.Entry::getValue)

            .collect(toList());

        log.info("Fund balances converted. Size: {}", fundBalances.size());
        return fundBalances;

    }

}
