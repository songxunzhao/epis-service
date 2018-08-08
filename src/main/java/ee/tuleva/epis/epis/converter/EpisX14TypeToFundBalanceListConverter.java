package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.account.FundBalance;
import ee.tuleva.epis.epis.exception.EpisMessageException;
import ee.x_road.epis.producer.AnswerType;
import ee.x_road.epis.producer.EpisX14ResponseType;
import ee.x_road.epis.producer.EpisX14Type;
import ee.x_road.epis.producer.ResultType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
public class EpisX14TypeToFundBalanceListConverter implements Converter<EpisX14Type, List<FundBalance>> {

  // FIXME: ugly hack to hotfix production
  static final List<String> IGNORED_3RD_PILLAR_FUND_ISINS = asList("EE3600109419", "EE3600010294", "EE3600098422",
      "EE3600109369", "EE3600074076", "EE3600008934", "EE3600007530", "EE3600071031", "EE3600071049");

    @Override
    public List<FundBalance> convert(EpisX14Type source) {
        log.info("Converting EpisX14Type to Fund Balance List");
        validateResult(source.getResponse().getResults());

        List<FundBalance> fundBalances = source.getResponse().getUnit().stream().map((EpisX14ResponseType.Unit unit) ->
                FundBalance.builder()
                .currency(unit.getCurrency())
                .isin(unit.getISIN())
                .value(unit.getAmount().multiply(unit.getNAV()))
                .pillar(2)
                .build())

                // Response might have duplicate elements
                .collect(toMap(FundBalance::getIsin, p -> p, (p, q) -> p))
                .entrySet().stream().map(Map.Entry::getValue)

                // FIXME: ugly hack to hotfix production
                .filter(fundBalance ->
                    IGNORED_3RD_PILLAR_FUND_ISINS.stream().noneMatch(isin -> isin.equalsIgnoreCase(fundBalance.getIsin()))
                )

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

}
