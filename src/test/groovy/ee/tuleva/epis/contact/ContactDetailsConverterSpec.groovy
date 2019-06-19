package ee.tuleva.epis.contact


import ee.x_road.epis.producer.*
import spock.lang.Specification

import static ee.tuleva.epis.config.UserPrincipalFixture.userPrincipalFixture
import static ee.tuleva.epis.contact.ContactDetails.ContactPreferenceType
import static ee.tuleva.epis.contact.ContactDetails.LanguagePreferenceType

class ContactDetailsConverterSpec extends Specification {

    ContactDetailsConverter converter = new ContactDetailsConverter()

    def "converts the epis response to contact details"() {
        given:
        AddressType address = new AddressType()
        address.setAddressRow1("Telliskivi 60")
        address.setAddressRow2("TALLINN")
        address.setAddressRow3("TALLINN")
        address.setCountry("EE")
        address.setPostalIndex("10412")
        address.setTerritory("0784")

        PersonType personalData = new PersonType()
        personalData.setContactPreference(MailType.E)
        personalData.setLanguagePreference(LangType.EST)
        personalData.setExtractFlag("N")
        personalData.setEMAIL("tuleva@tuleva.ee")

        EpisX12ResponseType response = new EpisX12ResponseType()
        response.setAddress(address)
        response.setPersonalData(personalData)

        PensionAccountType pensionAccountType = new PensionAccountType()
        pensionAccountType.setActiveISIN2("sampleActiveSecondPillarFundIsin")
        pensionAccountType.setPensionAccount("99800016777")
        response.setPensionAccount(pensionAccountType)

        EpisX12Type responseWrapper = new EpisX12Type()
        responseWrapper.setResponse(response)

        when:
        ContactDetails contactDetails = converter.convert(responseWrapper)

        then:
        contactDetails.addressRow1 == address.addressRow1
        contactDetails.addressRow2 == address.addressRow2
        contactDetails.addressRow3 == address.addressRow3
        contactDetails.country == address.country
        contactDetails.postalIndex == address.postalIndex
        contactDetails.districtCode == address.territory
        contactDetails.contactPreference == ContactPreferenceType.E
        contactDetails.languagePreference == LanguagePreferenceType.EST
        contactDetails.noticeNeeded == personalData.extractFlag
        contactDetails.email == personalData.EMAIL
        contactDetails.activeSecondPillarFundIsin == pensionAccountType.activeISIN2
        contactDetails.pensionAccountNumber == pensionAccountType.pensionAccount
    }

    def "converts when address does not exist"() {
        given:
        PersonType personalData = new PersonType()
        personalData.setContactPreference(MailType.E)
        personalData.setLanguagePreference(LangType.EST)
        personalData.setExtractFlag("N")
        personalData.setEMAIL("tuleva@tuleva.ee")

        EpisX12ResponseType response = new EpisX12ResponseType()
        response.setAddress(null)
        response.setPersonalData(personalData)

        PensionAccountType pensionAccountType = new PensionAccountType()
        pensionAccountType.setActiveISIN2("sampleActiveSecondPillarFundIsin")
        response.setPensionAccount(pensionAccountType)

        EpisX12Type responseWrapper = new EpisX12Type()
        responseWrapper.setResponse(response)

        when:
        ContactDetails contactDetails = converter.convert(responseWrapper)

        then:
        contactDetails.contactPreference == ContactPreferenceType.E
        contactDetails.languagePreference == LanguagePreferenceType.EST
        contactDetails.noticeNeeded == personalData.extractFlag
        contactDetails.email == personalData.EMAIL
        contactDetails.activeSecondPillarFundIsin == pensionAccountType.activeISIN2
    }

    def "converts when fields are null"() {
        given:
        PersonType personalData = new PersonType()
        personalData.setContactPreference(null)
        personalData.setLanguagePreference(null)
        personalData.setExtractFlag(null)
        personalData.setEMAIL(null)

        EpisX12ResponseType response = new EpisX12ResponseType()
        response.setAddress(null)
        response.setPersonalData(personalData)

        PensionAccountType pensionAccountType = new PensionAccountType()
        pensionAccountType.setActiveISIN2(null)
        response.setPensionAccount(pensionAccountType)

        EpisX12Type responseWrapper = new EpisX12Type()
        responseWrapper.setResponse(response)

        when:
        ContactDetails contactDetails = converter.convert(responseWrapper)

        then:
        contactDetails.contactPreference == ContactPreferenceType.E
        contactDetails.languagePreference == LanguagePreferenceType.EST
        contactDetails.noticeNeeded == 'Y'
        contactDetails.email == null
        contactDetails.activeSecondPillarFundIsin == null
    }

    def "converts when there is no personal data"() {
        given:
        EpisX12ResponseType response = new EpisX12ResponseType()
        response.setAddress(null)
        response.setPersonalData(null)

        PensionAccountType pensionAccountType = new PensionAccountType()
        pensionAccountType.setActiveISIN2(null)
        response.setPensionAccount(pensionAccountType)

        EpisX12Type responseWrapper = new EpisX12Type()
        responseWrapper.setResponse(response)

        when:
        ContactDetails contactDetails = converter.convert(responseWrapper)

        then:
        contactDetails.contactPreference == ContactPreferenceType.E
        contactDetails.languagePreference == LanguagePreferenceType.EST
        contactDetails.noticeNeeded == 'Y'
        contactDetails.email == null
        contactDetails.activeSecondPillarFundIsin == null
    }

    def "converts when there is no personal code, first name or last name"() {
        given:
        def principal = userPrincipalFixture()

        PersonType personalData = new PersonType()
        personalData.setPersonId(null)

        EpisX12ResponseType response = new EpisX12ResponseType()
        response.setPersonalData(personalData)

        EpisX12Type responseWrapper = new EpisX12Type()
        responseWrapper.setResponse(response)

        when:
        ContactDetails contactDetails = converter.convert(responseWrapper, principal)

        then:
        contactDetails.personalCode == principal.personalCode
        contactDetails.firstName == principal.firstName
        contactDetails.lastName == principal.lastName
    }
}
