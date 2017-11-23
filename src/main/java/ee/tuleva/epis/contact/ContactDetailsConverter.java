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

    ContactDetails.ContactDetailsBuilder builder = ContactDetails.builder();

    if (address != null) {
      builder
          .addressRow1(address.getAddressRow1())
          .addressRow2(address.getAddressRow2())
          .addressRow3(address.getAddressRow3())
          .country(address.getCountry())
          .postalIndex(address.getPostalIndex())
          .districtCode(address.getTerritory());
    }

    if (contactPreference != null) {
      builder
          .contactPreference(ContactDetails.ContactPreferenceType.valueOf(contactPreference.value()));
    }

    if (languagePreference != null) {
      builder
          .languagePreference(ContactDetails.LanguagePreferenceType.valueOf(languagePreference.value()));
    }

    return
        builder
            .noticeNeeded(personalData.getExtractFlag())
            .activeSecondPillarFundIsin(response.getResponse().getPensionAccount().getActiveISIN2())
            .build();
  }

}
