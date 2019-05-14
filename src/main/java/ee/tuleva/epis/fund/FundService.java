package ee.tuleva.epis.fund;

import ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory;
import ee.tuleva.epis.epis.EpisMessageWrapper;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.tuleva.epis.fund.converter.EpisX18TypeToFundListConverter;
import ee.x_road.epis.producer.EpisX18RequestType;
import ee.x_road.epis.producer.EpisX18Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FundService {

  private final EpisService episService;
  private final EpisMessageResponseStore episMessageResponseStore;
  private final EpisMessageWrapper episMessageWrapper;
  private final EpisMessageFactory episMessageFactory;
  private final EpisX18TypeToFundListConverter converter;

  @Cacheable(value = "funds", unless = "#result == null or #result.isEmpty()")
  public List<Fund> getPensionFunds() {
    log.info("Getting pension funds from EPIS");
    EpisMessage message = sendQuery();
    EpisX18Type response = episMessageResponseStore.pop(message.getId(), EpisX18Type.class);
    return converter.convert(response);
  }

  private EpisMessage sendQuery() {
    EpisX18RequestType request = episMessageFactory.createEpisX18RequestType();

    EpisX18Type episX18Type = episMessageFactory.createEpisX18Type();
    episX18Type.setRequest(request);

    JAXBElement<EpisX18Type> pensionFundListRequest = episMessageFactory.createPENSIONIFONDID(episX18Type);

    String id = UUID.randomUUID().toString().replace("-", "");
    Ex ex = episMessageWrapper.wrap(id, pensionFundListRequest);

    EpisMessage episMessage = EpisMessage.builder()
        .payload(ex)
        .id(id)
        .build();

    episService.send(episMessage.getPayload());
    return episMessage;
  }

}
