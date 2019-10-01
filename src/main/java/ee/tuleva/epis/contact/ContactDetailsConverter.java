package ee.tuleva.epis.contact;

import ee.tuleva.epis.config.UserPrincipal;
import ee.tuleva.epis.contact.ContactDetails.ContactPreferenceType;
import ee.tuleva.epis.contact.ContactDetails.Distribution;
import ee.tuleva.epis.contact.ContactDetails.LanguagePreferenceType;
import ee.x_road.epis.producer.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
public class ContactDetailsConverter implements Converter<EpisX12Type, ContactDetails> {

    @Override
    @NonNull
    public ContactDetails convert(EpisX12Type responseWrapper) {
        return convert(responseWrapper, new UserPrincipal());
    }

    @NonNull
    public ContactDetails convert(EpisX12Type responseWrapper, UserPrincipal principal) {
        EpisX12ResponseType response = responseWrapper.getResponse();

        AddressType address = response.getAddress() != null ? response.getAddress() : emptyAddress();

        PersonType personalData = response.getPersonalData() != null ? response.getPersonalData() : emptyPersonalData();

        String firstName = personalData.getFirstName() != null ? personalData.getFirstName() : principal.getFirstName();

        String lastName = personalData.getName() != null ? personalData.getName() : principal.getLastName();

        String personalCode = personalData.getPersonId() != null ? personalData.getPersonId() : principal.getPersonalCode();

        PensionAccountType pensionAccount = response.getPensionAccount() != null ?
            response.getPensionAccount() : emptyPensionAccount();

        MailType contactPreference = personalData.getContactPreference() != null ?
            personalData.getContactPreference() : (personalData.getEMAIL() != null ? MailType.E : MailType.P);

        LangType languagePreference = personalData.getLanguagePreference() != null ?
            personalData.getLanguagePreference() : LangType.EST;

        String extractFlag = personalData.getExtractFlag() != null ? personalData.getExtractFlag() : "Y";

        List<PensionAccountType.Distribution> distributions = pensionAccount.getDistribution() != null ?
            pensionAccount.getDistribution() : emptyList();

        List<Distribution> thirdPillarDistributions = distributions.stream()
            .map(distribution -> new Distribution(distribution.getActiveISIN3(), distribution.getPercentage()))
            .collect(toList());

        boolean isSecondPillarActive = pensionAccount.getActiveDate2() != null;

        boolean isThirdPillarActive = pensionAccount.getActiveDate3() != null;

        return ContactDetails.builder()
            .firstName(firstName)
            .lastName(lastName)
            .personalCode(personalCode)
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
            .isSecondPillarActive(isSecondPillarActive)
            .isThirdPillarActive(isThirdPillarActive)
            .build();
    }

    private AddressType emptyAddress() {
        return new AddressType();
    }

    private PensionAccountType emptyPensionAccount() {
        return new PensionAccountType();
    }

    private PersonType emptyPersonalData() {
        return new PersonType();
    }
}
