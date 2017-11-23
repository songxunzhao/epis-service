package ee.tuleva.epis.account;

import ee.x_road.epis.producer.EpisX14ResponseType;
import ee.x_road.epis.producer.EpisX14Type;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
public class EpisX14TypeToFundBalanceListConverter implements Converter<EpisX14Type, List<FundBalance>> {

    @Override
    public List<FundBalance> convert(EpisX14Type source) {
        log.info("Converting EpisX14Type to Fund Balance List");
        List<FundBalance> fundBalances = source.getResponse().getUnit().stream().map((EpisX14ResponseType.Unit unit) -> FundBalance.builder()
            .currency(unit.getCurrency())
            .isin(unit.getISIN())
            .value(unit.getAmount().multiply(unit.getNAV()))
            .pillar(2)
            .build())

            // Response might have duplicate elements
            .collect(toMap(FundBalance::getIsin, p -> p, (p, q) -> p))
            .entrySet().stream().map(Map.Entry::getValue)

            .collect(toList());
        log.info("Fund Balances: {}", fundBalances);
        return fundBalances;

    }

}
