package ee.tuleva.epis.contact;

import ee.tuleva.epis.contact.ContactDetails.ContactPreferenceType;
import ee.tuleva.epis.contact.ContactDetails.Distribution;
import ee.tuleva.epis.contact.ContactDetails.LanguagePreferenceType;
import ee.x_road.epis.producer.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
public class ContactDetailsConverter {

    ContactDetails toContactDetails(EpisX12Type responseWrapper) {
        EpisX12ResponseType response = responseWrapper.getResponse();

        AddressType address = clean(response.getAddress());

        PersonType personalData = response.getPersonalData() != null ? response.getPersonalData() : emptyPersonalData();

        PensionAccountType pensionAccount = response.getPensionAccount() != null ?
            response.getPensionAccount() : emptyPensionAccount();

        MailType contactPreference = personalData.getContactPreference() != null ?
            personalData.getContactPreference() : MailType.E;

        LangType languagePreference = personalData.getLanguagePreference() != null ?
            personalData.getLanguagePreference() : LangType.EST;

        String extractFlag = personalData.getExtractFlag() != null ? personalData.getExtractFlag() : "Y";

        List<PensionAccountType.Distribution> distributions = pensionAccount.getDistribution() != null ?
            pensionAccount.getDistribution() : emptyList();

        List<Distribution> thirdPillarDistributions = distributions.stream()
            .map(distribution -> new Distribution(distribution.getActiveISIN3(), distribution.getPercentage()))
            .collect(toList());

        return ContactDetails.builder()
            .firstName(personalData.getFirstName())
            .lastName(personalData.getName())
            .personalCode(personalData.getPersonId())
            .addressRow1(address.getAddressRow1())
            .addressRow2(address.getAddressRow2())
            .addressRow3(address.getAddressRow3())
            .country(address.getCountry())
            .postalIndex(address.getPostalIndex())
            .districtCode(address.getTerritory())
            .contactPreference(ContactPreferenceType.valueOf(contactPreference.value()))
            .languagePreference(LanguagePreferenceType.valueOf(languagePreference.value()))
            .noticeNeeded(extractFlag)
            .email(personalData.getEMAIL())
            .phoneNumber(personalData.getPhone())
            .activeSecondPillarFundIsin(pensionAccount.getActiveISIN2())
            .pensionAccountNumber(pensionAccount.getPensionAccount())
            .thirdPillarDistribution(thirdPillarDistributions)
            .build();
    }

    private AddressType clean(AddressType addressType) {
        AddressType address = addressType != null ? addressType : emptyAddress();
        address.setCountry(address.getCountry() != null ? address.getCountry() : "EE");
        if (isMissing(address)) {
            return defaultAddress();
        }
        return address;
    }

    private boolean isMissing(AddressType address) {
        return Stream.of(
            address.getAddressRow1(),
            address.getTerritory(),
            address.getPostalIndex())
            .anyMatch(str -> str == null || str.isEmpty());
    }

    private AddressType emptyAddress() {
        return new AddressType();
    }

    static AddressType defaultAddress() {
        AddressType address = new AddressType();
        address.setAddressRow1("Tuleva, Telliskivi 60");
        address.setCountry("EE");
        address.setPostalIndex("10412");
        address.setTerritory("0784");
        return address;
    }

    private PensionAccountType emptyPensionAccount() {
        return new PensionAccountType();
    }

    private PersonType emptyPersonalData() {
        return new PersonType();
    }

}
