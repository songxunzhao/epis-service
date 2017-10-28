package ee.tuleva.epis.contact;

import ee.tuleva.epis.epis.EpisMessageWrapper;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.x_road.epis.producer.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactDetailsService {

  private final EpisService episService;
  private final EpisMessageResponseStore episMessageResponseStore;
  private final EpisMessageWrapper episMessageWrapper;
  private final ContactDetailsConverter contactDetailsConverter;

  //TODO: caching short lived
  public ContactDetails get(String personalCode) {
    EpisMessage message = sendQuery(personalCode);
    EpisX12Type response = episMessageResponseStore.pop(message.getId(), EpisX12Type.class);
    return contactDetailsConverter.toContactDetails(response);
  }

  private EpisMessage sendQuery(String personalCode) {
    ee.x_road.epis.producer.ObjectFactory episFactory = new ee.x_road.epis.producer.ObjectFactory();

    PersonDataRequestType personalData = episFactory.createPersonDataRequestType();
    personalData.setPersonId(personalCode);

    EpisX12RequestType request = episFactory.createEpisX12RequestType();
    request.setPersonalData(personalData);

    EpisX12Type episX12Type = episFactory.createEpisX12Type();
    episX12Type.setRequest(request);

    JAXBElement<EpisX12Type> personalDataRequest = episFactory.createISIKUANDMED(episX12Type);
    String id = UUID.randomUUID().toString().replace("-", "");
    Ex ex = episMessageWrapper.wrap(id, personalDataRequest);

    EpisMessage episMessage = EpisMessage.builder()
        .payload(ex)
        .id(id)
        .build();

    episService.send(episMessage.getPayload());
    return episMessage;
  }
}
