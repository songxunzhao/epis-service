package ee.tuleva.epis.contact

import ee.x_road.epis.producer.AddressType
import ee.x_road.epis.producer.EpisX12ResponseType
import ee.x_road.epis.producer.EpisX12Type
import ee.x_road.epis.producer.LangType
import ee.x_road.epis.producer.MailType
import ee.x_road.epis.producer.PensionAccountType
import ee.x_road.epis.producer.PersonType
import spock.lang.Specification

class ContactDetailsConverterSpec extends Specification {

  def converter = new ContactDetailsConverter()

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

    EpisX12ResponseType response = new EpisX12ResponseType()
    response.setAddress(address)
    response.setPersonalData(personalData)

    PensionAccountType pensionAccountType = new PensionAccountType()
    pensionAccountType.setActiveISIN2("sampleActiveSecondPillarFundIsin")
    response.setPensionAccount(pensionAccountType)

    EpisX12Type sampleResponse = new EpisX12Type()
    sampleResponse.setResponse(response)

    when:
    ContactDetails contactDetails = converter.toContactDetails(sampleResponse)

    then:
    contactDetails.addressRow1 == address.addressRow1
    contactDetails.addressRow2 == address.addressRow2
    contactDetails.addressRow3 == address.addressRow3
    contactDetails.country == address.country
    contactDetails.postalIndex == address.postalIndex
    contactDetails.districtCode == address.territory
    contactDetails.contactPreference == ContactDetails.ContactPreferenceType.E
    contactDetails.languagePreference == ContactDetails.LanguagePreferenceType.EST
    contactDetails.noticeNeeded == personalData.extractFlag
    contactDetails.activeSecondPillarFundIsin == pensionAccountType.activeISIN2
  }

  def "converts when address does not exist"() {
    given:
    PersonType personalData = new PersonType()
    personalData.setContactPreference(MailType.E)
    personalData.setLanguagePreference(LangType.EST)
    personalData.setExtractFlag("N")

    EpisX12ResponseType response = new EpisX12ResponseType()
    response.setAddress(null)
    response.setPersonalData(personalData)

    PensionAccountType pensionAccountType = new PensionAccountType()
    pensionAccountType.setActiveISIN2("sampleActiveSecondPillarFundIsin")
    response.setPensionAccount(pensionAccountType)

    EpisX12Type sampleResponse = new EpisX12Type()
    sampleResponse.setResponse(response)

    when:
    ContactDetails contactDetails = converter.toContactDetails(sampleResponse)

    then:
    contactDetails.contactPreference == ContactDetails.ContactPreferenceType.E
    contactDetails.languagePreference == ContactDetails.LanguagePreferenceType.EST
    contactDetails.noticeNeeded == personalData.extractFlag
    contactDetails.activeSecondPillarFundIsin == pensionAccountType.activeISIN2
  }

  def "converts when fields are null"() {
    given:
    PersonType personalData = new PersonType()
    personalData.setContactPreference(null)
    personalData.setLanguagePreference(null)
    personalData.setExtractFlag(null)

    EpisX12ResponseType response = new EpisX12ResponseType()
    response.setAddress(null)
    response.setPersonalData(personalData)

    PensionAccountType pensionAccountType = new PensionAccountType()
    pensionAccountType.setActiveISIN2(null)
    response.setPensionAccount(pensionAccountType)

    EpisX12Type sampleResponse = new EpisX12Type()
    sampleResponse.setResponse(response)

    when:
    ContactDetails contactDetails = converter.toContactDetails(sampleResponse)

    then:
    contactDetails.contactPreference == null
    contactDetails.languagePreference == null
    contactDetails.noticeNeeded == null
    contactDetails.activeSecondPillarFundIsin == null
  }
}
