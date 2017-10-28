package ee.tuleva.epis.person

import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.x_road.epis.producer.*
import spock.lang.Specification

import javax.xml.bind.JAXBElement

class PersonServiceSpec extends Specification {

    EpisService episService = Mock(EpisService);
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore);
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    PersonService service = new PersonService(episService,
            episMessageResponseStore, episMessageWrapper)

    def "Get person"() {
        given:
        String personalCode = "38080808080"

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

        EpisX12Type sampleResponse = new EpisX12Type()
        sampleResponse.setResponse(response)

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        })

        episMessageResponseStore.pop(_, EpisX12Type.class) >> sampleResponse

        when:
        Person person = service.get(personalCode)

        then:
        person.addressRow1 == address.addressRow1
        person.addressRow2 == address.addressRow2
        person.addressRow3 == address.addressRow3
        person.country == address.country
        person.postalIndex == address.postalIndex
        person.districtCode == address.territory
        person.contactPreference == Person.ContactPreferenceType.E
        person.languagePreference == Person.LanguagePreferenceType.EST
        person.noticeNeeded == personalData.extractFlag
    }

}
