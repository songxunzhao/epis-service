package ee.tuleva.epis.person;

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
public class PersonService {

  private final EpisService episService;
  private final EpisMessageResponseStore episMessageResponseStore;
  private final EpisMessageWrapper episMessageWrapper;

  Person get(String personalCode) {
    EpisMessage message = sendQuery(personalCode);
    EpisX12Type response = episMessageResponseStore.pop(message.getId(), EpisX12Type.class);
    return toPerson(response);
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

  private Person toPerson(EpisX12Type response) {
    AddressType address = response.getResponse().getAddress();
    PersonType personalData = response.getResponse().getPersonalData();
    MailType contactPreference = personalData.getContactPreference();
    LangType languagePreference = personalData.getLanguagePreference();

    return Person.builder()
        .addressRow1(address.getAddressRow1())
        .addressRow2(address.getAddressRow2())
        .addressRow3(address.getAddressRow3())
        .country(address.getCountry())
        .postalIndex(address.getPostalIndex())
        .districtCode(address.getTerritory())
        .contactPreference(Person.ContactPreferenceType.valueOf(contactPreference.value()))
        .languagePreference(Person.LanguagePreferenceType.valueOf(languagePreference.value()))
        .noticeNeeded(personalData.getExtractFlag())
        .build();
  }
}
