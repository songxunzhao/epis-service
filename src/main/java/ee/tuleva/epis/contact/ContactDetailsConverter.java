package ee.tuleva.epis.contact;

import ee.x_road.epis.producer.*;
import org.springframework.stereotype.Service;

@Service
public class ContactDetailsConverter {

    ContactDetails toContactDetails(EpisX12Type responseWrapper) {
        EpisX12ResponseType response = responseWrapper.getResponse();
        AddressType address = response.getAddress();

        PersonType personalData = response.getPersonalData();
        if (personalData == null) {
            personalData = emptyPersonalData();
        }

        MailType contactPreference = personalData.getContactPreference();
        LangType languagePreference = personalData.getLanguagePreference();

        PensionAccountType pensionAccount = response.getPensionAccount();
        if (pensionAccount == null) {
            pensionAccount = emptyPensionAccount();
        }

        ContactDetails.ContactDetailsBuilder builder = ContactDetails.builder();

        builder.firstName(personalData.getFirstName());
        builder.lastName(personalData.getName());
        builder.personalCode(personalData.getPersonId());

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
                .email(personalData.getEMAIL())
                .activeSecondPillarFundIsin(pensionAccount.getActiveISIN2())
                .build();
    }

    private PensionAccountType emptyPensionAccount() {
        return new PensionAccountType();
    }

    private PersonType emptyPersonalData() {
        return new PersonType();
    }

}
