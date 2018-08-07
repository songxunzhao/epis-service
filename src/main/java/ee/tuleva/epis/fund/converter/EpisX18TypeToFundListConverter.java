package ee.tuleva.epis.fund.converter;


import ee.tuleva.epis.fund.Fund;
import ee.tuleva.epis.fund.Fund.FundStatus;
import ee.x_road.epis.producer.EpisX18ResponseType.Security;
import ee.x_road.epis.producer.EpisX18Type;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@Slf4j
public class EpisX18TypeToFundListConverter implements Converter<EpisX18Type, List<Fund>> {

  @Override
  public List<Fund> convert(EpisX18Type source) {
    return source.getResponse().getSecurity().stream()
        .map(this::securityToFund)
        .collect(toList());
  }

  private Fund securityToFund(Security security) {
    return Fund.builder()
    .isin(security.getISIN())
    .name(security.getName())
    .shortName(security.getShortName())
    .pillar(Integer.parseInt(security.getPillar()))
    .status(FundStatus.parse(security.getStatus().value()))
    .build();
  }
}
