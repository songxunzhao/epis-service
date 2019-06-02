package ee.tuleva.epis.contact

import static ee.tuleva.epis.contact.ContactDetails.*

class ContactDetailsFixture {

    static ContactDetails contactDetailsFixture() {
        return builder()
            .firstName("Peeter")
            .lastName("Meeter")
            .personalCode("3123456778")
            .addressRow1("Tuleva, Telliskivi 60")
            .addressRow2("TALLINN")
            .addressRow3("TALLINN")
            .country("EE")
            .postalIndex("10412")
            .districtCode("0784")
            .contactPreference(ContactPreferenceType.valueOf("E"))
            .languagePreference(LanguagePreferenceType.valueOf("EST"))
            .noticeNeeded("Y")
            .email("tuleva@tuleva.ee")
            .activeSecondPillarFundIsin("EE3600109435")
            .phoneNumber("431243254")
            .thirdPillarDistribution([new Distribution("EE3600109419", 1.0)])
            .pensionAccountNumber("99800016991")
            .build()
    }
}
