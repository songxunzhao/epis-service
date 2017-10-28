package ee.tuleva.epis.contact;

import ee.x_road.epis.producer.*;
import org.springframework.stereotype.Service;

@Service
public class ContactDetailsConverter {

  ContactDetails toContactDetails(EpisX12Type response) {
    AddressType address = response.getResponse().getAddress();
    PersonType personalData = response.getResponse().getPersonalData();
    MailType contactPreference = personalData.getContactPreference();
    LangType languagePreference = personalData.getLanguagePreference();

    return ContactDetails.builder()
        .addressRow1(address.getAddressRow1())
        .addressRow2(address.getAddressRow2())
        .addressRow3(address.getAddressRow3())
        .country(address.getCountry())
        .postalIndex(address.getPostalIndex())
        .districtCode(address.getTerritory())
        .contactPreference(ContactDetails.ContactPreferenceType.valueOf(contactPreference.value()))
        .languagePreference(ContactDetails.LanguagePreferenceType.valueOf(languagePreference.value()))
        .noticeNeeded(personalData.getExtractFlag())
        .build();
  }

}
